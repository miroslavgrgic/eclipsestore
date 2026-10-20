// Tool-calling agent loop on top of a local chat model: the model is given
// the MCP tool definitions through its chat template, and replies either with
// <tool_call>{...}</tool_call> blocks (Qwen / Hermes style) - which are run
// against the MCP server and fed back as tool messages - or with the final
// answer. Free of DOM and model dependencies (both are injected), so it also
// runs under Node for testing.

import { toolResultText } from './mcp.js';

const MAX_STEPS = 4;
// Prompts must stay short: besides being slow, prompts of roughly 3000+
// tokens make the WebGPU runtime fail (it returns the logits of every prompt
// position, which outgrows the mappable buffer size). The tool definitions
// alone take ~1500 tokens. So only this much of a tool result is shown to the
// model (the full payload still goes to the data graph), and only a short
// history is kept.
const MAX_TOOL_RESULT_CHARS = 3000;
const MAX_HISTORY_TURNS = 2;
const MAX_HISTORY_MESSAGE_CHARS = 600;

export function systemPrompt(serverName, instructions) {
  const today = new Date().toISOString().slice(0, 10);
  return [
    `You are a helpful data assistant connected to the MCP server "${serverName || 'MCP server'}".`,
    `Today is ${today}.`,
    'Whenever a question concerns data, call one of the provided tools to look it up - never invent data.',
    'Pass dates as yyyy-mm-dd. For a whole month, pass its first and last day (e.g. from 2026-03-01 to 2026-03-31).',
    'Omit optional tool arguments you do not need.',
    'Once you have the tool results, answer concisely in Markdown, using a table for lists of records.',
    instructions ? `Server instructions: ${instructions}` : '',
  ].filter(Boolean).join('\n');
}

export function toToolDefinitions(mcpTools) {
  return mcpTools.map(tool => ({
    type: 'function',
    function: {
      name: tool.name,
      description: tool.description || '',
      parameters: tool.inputSchema || { type: 'object', properties: {} },
    },
  }));
}

const TOOL_CALL_RE = /<tool_call>\s*([\s\S]*?)\s*(?:<\/tool_call>|$)/g;
const THINK_RE = /<think>[\s\S]*?(?:<\/think>|$)/g;

export function stripThinking(text) {
  return text.replace(THINK_RE, '').trim();
}

// Text the user should see while/after generating: anything before the
// first tool call, without reasoning blocks.
export function visibleText(text) {
  const withoutThinking = stripThinking(text);
  const start = withoutThinking.indexOf('<tool_call>');
  return (start === -1 ? withoutThinking : withoutThinking.slice(0, start)).trim();
}

export function parseToolCalls(text) {
  const calls = [];
  const cleaned = stripThinking(text);
  for (const match of cleaned.matchAll(TOOL_CALL_RE)) {
    const call = parseCallJson(match[1]);
    if (call) calls.push(call);
  }
  // Some models skip the tags and emit the bare call object.
  if (calls.length === 0 && /^\s*\{[\s\S]*"name"[\s\S]*\}\s*$/.test(cleaned)) {
    const call = parseCallJson(cleaned);
    if (call) calls.push(call);
  }
  return calls;
}

function parseCallJson(raw) {
  let parsed;
  try {
    parsed = JSON.parse(raw);
  } catch (err) {
    return null;
  }
  if (!parsed || typeof parsed.name !== 'string') return null;
  let args = parsed.arguments ?? parsed.parameters ?? {};
  if (typeof args === 'string') {
    try { args = JSON.parse(args); } catch (err) { args = {}; }
  }
  return { name: parsed.name, arguments: args && typeof args === 'object' ? args : {} };
}

function truncate(text, max) {
  return text.length <= max ? text : text.slice(0, max) + `\n…[truncated ${text.length - max} characters]`;
}

/**
 * Answers one user message.
 *
 * @param {object} options
 * @param {string} options.message      the user's question
 * @param {Array}  options.history      earlier [{role, content}] turns (user / assistant only)
 * @param {Array}  options.tools        MCP tool descriptors (from tools/list)
 * @param {string} options.system       system prompt
 * @param {Function} options.generate   async (messages, toolDefinitions, onToken, prefill) => generated text
 *                                      (prefill: text the reply must start with; not part of the returned text)
 * @param {Function} options.callTool   async (name, args) => MCP tools/call result
 * @param {Function} [options.onEvent]  progress callback: {type: 'tool-start' | 'tool-end' | 'token', ...}
 * @param {boolean} [options.forceFirstToolCall]  start the first reply with <tool_call> - small models
 *                                      otherwise tend to answer data questions from imagination
 * @returns {Promise<{reply: string, usedTools: string[], toolCalls: Array}>}
 */
export async function runAgent({ message, history, tools, system, generate, callTool, onEvent = () => {}, forceFirstToolCall = false }) {
  const toolDefinitions = toToolDefinitions(tools);
  const toolNames = new Set(tools.map(tool => tool.name));
  const messages = [
    { role: 'system', content: system },
    ...history.slice(-MAX_HISTORY_TURNS * 2).map(turn => ({ ...turn, content: truncate(turn.content, MAX_HISTORY_MESSAGE_CHARS) })),
    { role: 'user', content: message },
  ];
  const usedTools = [];
  const toolCalls = [];
  const seenCalls = new Set();
  // Once a tool call succeeded, or the model starts repeating itself, tools
  // are withheld: the model then has to answer from the results (and the
  // prompt is ~1500 tokens shorter). The same goes for the last step.
  let toolsWithheld = false;

  for (let step = 0; step < MAX_STEPS; step++) {
    const offeredTools = step === MAX_STEPS - 1 || toolsWithheld ? [] : toolDefinitions;
    const prefill = step === 0 && forceFirstToolCall && offeredTools.length > 0 ? '<tool_call>\n' : '';
    let streamed = prefill;
    const onToken = token => {
      onEvent({ type: 'token', text: streamed + token });
      streamed = '';
    };
    const text = prefill + await generate(messages, offeredTools, onToken, prefill);
    const parsedCalls = parseToolCalls(text);
    const fresh = parsedCalls
      .filter(call => toolNames.has(call.name))
      .filter(call => !seenCalls.has(JSON.stringify(call)));
    if (fresh.length === 0) {
      // Only repeated / unknown tool calls: the results are already there, so
      // ask once more without tools to get the answer out of the model.
      if (parsedCalls.length > 0 && !toolsWithheld && step < MAX_STEPS - 1) {
        toolsWithheld = true;
        continue;
      }
      const reply = visibleText(text) ||
        (parsedCalls.length > 0 ? 'I could not find an answer with the available tools.' : stripThinking(text));
      return { reply, usedTools, toolCalls };
    }

    messages.push({ role: 'assistant', content: stripThinking(text) });
    for (const call of fresh) {
      seenCalls.add(JSON.stringify(call));
      onEvent({ type: 'tool-start', name: call.name, arguments: call.arguments });
      let resultText;
      try {
        const result = await callTool(call.name, call.arguments);
        resultText = toolResultText(result);
        usedTools.push(call.name);
        toolCalls.push({
          name: call.name,
          input: JSON.stringify(call.arguments),
          output: JSON.stringify(result.content || []),
        });
        if (result.isError) {
          resultText = `Tool error: ${resultText}\nCorrect the arguments and call the tool again.`;
        } else {
          toolsWithheld = true;
        }
        onEvent({ type: 'tool-end', name: call.name, isError: !!result.isError });
      } catch (err) {
        resultText = 'Tool call failed: ' + err.message;
        onEvent({ type: 'tool-end', name: call.name, isError: true, error: err.message });
      }
      messages.push({ role: 'tool', content: truncate(resultText || '(empty result)', MAX_TOOL_RESULT_CHARS) });
    }
  }
  return { reply: 'I could not complete the answer within the allowed number of tool calls.', usedTools, toolCalls };
}
