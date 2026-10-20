import { McpClient } from './mcp.js';
import { runAgent, systemPrompt, visibleText } from './agent.js';
import { initGraph, renderGraph, showGraphFor } from './graph.js';

const messagesEl = document.getElementById('messages');
const formEl = document.getElementById('chat-form');
const inputEl = document.getElementById('chat-input');
const submitEl = document.getElementById('chat-submit');
const toolsPanelEl = document.getElementById('tools-panel');
const toolsListEl = document.getElementById('tools-list');
const toolsSummaryEl = toolsPanelEl.querySelector('summary');
const mcpDotEl = document.getElementById('mcp-dot');
const mcpStatusEl = document.getElementById('mcp-status');
const modelDotEl = document.getElementById('model-dot');
const modelStatusEl = document.getElementById('model-status');
const modelProgressEl = document.getElementById('model-progress');
const modelProgressBarEl = modelProgressEl.querySelector('.model-progress-bar');
const modelSelectEl = document.getElementById('model-select');

// The MCP endpoint is same-origin: server.js proxies /mcp to the real MCP
// server (localhost:8080 by default), which sends no CORS headers itself.
// Override with ?mcp=<url> to talk to a CORS-enabled server directly.
const params = new URLSearchParams(location.search);
const MCP_URL = params.get('mcp') || '/mcp';
// ?device=webgpu|wasm and ?dtype=q4|q4f16|q8|fp16 override the automatic choice.
const DEVICE_OVERRIDE = params.get('device');
const DTYPE_OVERRIDE = params.get('dtype');

// Chat models with tool calling support in their chat template, available
// as ONNX builds for Transformer.js.
//   webgpuDtype      weight format on WebGPU (default q4, see llm-worker.js)
//   toolCallPrefill  opening of a tool call in the model's own format; the
//                    first reply is forced to start with it, so the model
//                    looks the data up instead of inventing an answer
//   templateOptions  extra chat template variables
//   modelOptions     extra from_pretrained() options
const QWEN_TOOL_CALL = '<tool_call>\n';
const LLAMA_TOOL_CALL = '{"name": "';
const MODELS = [
  { id: 'onnx-community/Qwen3-0.6B-ONNX', label: 'Qwen3 0.6B', webgpuDtype: 'q4f16', toolCallPrefill: QWEN_TOOL_CALL, templateOptions: { enable_thinking: false } },
  { id: 'onnx-community/Qwen2.5-0.5B-Instruct', label: 'Qwen2.5 0.5B Instruct', toolCallPrefill: QWEN_TOOL_CALL },
  // For machines with a strong GPU - not tested. Their weights come as
  // external data files (the format Hugging Face's WebGPU demos use for
  // models of this size), split into the given number of chunks.
  { id: 'onnx-community/Llama-3.2-1B-Instruct-ONNX', label: 'Llama 3.2 1B Instruct (1.1 GB, untested)', webgpuDtype: 'q4f16', toolCallPrefill: LLAMA_TOOL_CALL, modelOptions: { use_external_data_format: 1 } },
  { id: 'onnx-community/Llama-3.2-3B-Instruct-ONNX', label: 'Llama 3.2 3B Instruct (2.4 GB, untested)', webgpuDtype: 'q4f16', toolCallPrefill: LLAMA_TOOL_CALL, modelOptions: { use_external_data_format: 2 } },
  { id: 'onnx-community/Qwen3-4B-ONNX', label: 'Qwen3 4B (2.8 GB, untested)', webgpuDtype: 'q4f16', toolCallPrefill: QWEN_TOOL_CALL, templateOptions: { enable_thinking: false }, modelOptions: { use_external_data_format: 2 } },
  // Not offered: Qwen2.5 1.5B and Qwen3 1.7B ship as single files of 1.4 GB
  // and more, which fail to load in the browser (std::bad_alloc), and
  // Qwen2.5 breaks with fp16 weights - see README.
];
const MODEL_STORAGE_KEY = 'mcp-browser-client.model';

// Consistent, distinct colors per MCP tool name, shared between the chat
// message badges and the tools panel.
const TOOL_COLORS = [
  { bg: 'rgba(99, 102, 241, 0.16)', fg: '#6366f1' },  // indigo
  { bg: 'rgba(236, 72, 153, 0.16)', fg: '#db2777' },  // pink
  { bg: 'rgba(168, 85, 247, 0.16)', fg: '#a855f7' },  // purple
  { bg: 'rgba(20, 184, 166, 0.16)', fg: '#0d9488' },  // teal
  { bg: 'rgba(249, 115, 22, 0.16)', fg: '#ea580c' },  // orange
  { bg: 'rgba(34, 197, 94, 0.16)', fg: '#16a34a' },   // green
  { bg: 'rgba(59, 130, 246, 0.16)', fg: '#2563eb' },  // blue
];

function colorForTool(name) {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = (hash * 31 + name.charCodeAt(i)) >>> 0;
  }
  return TOOL_COLORS[hash % TOOL_COLORS.length];
}

function setStatus(dotEl, valueEl, state, text) {
  dotEl.dataset.state = state;
  valueEl.textContent = text;
  valueEl.title = text;
}

// ---- MCP --------------------------------------------------------------

const mcp = new McpClient(MCP_URL);
let mcpTools = [];
let mcpReady = null;

function connectMcp() {
  setStatus(mcpDotEl, mcpStatusEl, 'pending', 'connecting…');
  mcpReady = (async () => {
    try {
      await mcp.connect();
      mcpTools = await mcp.listTools();
      const name = (mcp.serverInfo && mcp.serverInfo.name) || 'connected';
      setStatus(mcpDotEl, mcpStatusEl, 'ok', name);
      renderTools(mcpTools);
    } catch (err) {
      setStatus(mcpDotEl, mcpStatusEl, 'error', 'unreachable');
      renderToolsError(err);
      throw err;
    }
  })();
  mcpReady.catch(() => {});
}

function formatSchema(inputSchema) {
  if (typeof inputSchema !== 'string') return JSON.stringify(inputSchema, null, 2);
  try {
    return JSON.stringify(JSON.parse(inputSchema), null, 2);
  } catch (err) {
    return inputSchema;
  }
}

function renderTools(tools) {
  toolsSummaryEl.textContent = `Available MCP tools (${tools.length})`;
  toolsListEl.innerHTML = '';

  if (tools.length === 0) {
    const empty = document.createElement('div');
    empty.className = 'tool-card';
    empty.textContent = 'No MCP tools discovered.';
    toolsListEl.appendChild(empty);
    return;
  }

  for (const tool of tools) {
    const card = document.createElement('div');
    card.className = 'tool-card';
    const color = colorForTool(tool.name);
    card.style.borderLeftColor = color.fg;

    const name = document.createElement('div');
    name.className = 'tool-name';
    name.style.color = color.fg;
    name.textContent = tool.name;
    card.appendChild(name);

    if (tool.description) {
      const desc = document.createElement('p');
      desc.className = 'tool-desc';
      desc.textContent = tool.description;
      card.appendChild(desc);
    }

    if (tool.inputSchema) {
      const schemaDetails = document.createElement('details');
      const schemaSummary = document.createElement('summary');
      schemaSummary.textContent = 'Parameters';
      const pre = document.createElement('pre');
      pre.textContent = formatSchema(tool.inputSchema);
      schemaDetails.appendChild(schemaSummary);
      schemaDetails.appendChild(pre);
      card.appendChild(schemaDetails);
    }

    toolsListEl.appendChild(card);
  }
}

function renderToolsError(err) {
  toolsSummaryEl.textContent = 'Available MCP tools';
  toolsListEl.innerHTML = '';
  const error = document.createElement('div');
  error.className = 'tool-card';
  error.textContent = `Failed to load tools from ${MCP_URL}: ${err.message}`;
  const retry = document.createElement('button');
  retry.type = 'button';
  retry.className = 'graph-btn retry-btn';
  retry.textContent = 'Retry';
  retry.addEventListener('click', connectMcp);
  error.appendChild(retry);
  toolsListEl.appendChild(error);
}

// ---- Local model (runs in llm-worker.js) -------------------------------

const worker = new Worker(new URL('./llm-worker.js', import.meta.url), { type: 'module' });
let modelReady = null;
let currentModel = null;
let modelLoading = false;
let modelFailed = false;
let resolveModelReady = null;
let rejectModelReady = null;
const generations = new Map();
let nextGenerationId = 1;

function formatBytes(bytes) {
  return bytes >= 1e9 ? (bytes / 1e9).toFixed(2) + ' GB' : Math.round(bytes / 1e6) + ' MB';
}

worker.addEventListener('message', ({ data }) => {
  if (data.type === 'progress') {
    const ratio = data.total ? data.loaded / data.total : 0;
    modelProgressEl.hidden = false;
    modelProgressBarEl.style.width = (ratio * 100).toFixed(1) + '%';
    setStatus(modelDotEl, modelStatusEl, 'pending',
      ratio >= 1 ? 'initializing…' : `downloading ${formatBytes(data.loaded)} / ${formatBytes(data.total)}`);
  } else if (data.type === 'ready') {
    modelProgressEl.hidden = true;
    modelLoading = false;
    modelSelectEl.disabled = busy;
    setStatus(modelDotEl, modelStatusEl, 'ok', `ready · ${data.device === 'webgpu' ? 'WebGPU' : 'WASM'}`);
    resolveModelReady();
  } else if (data.type === 'token') {
    const generation = generations.get(data.id);
    if (generation) generation.onToken(data.text);
  } else if (data.type === 'done') {
    const generation = generations.get(data.id);
    generations.delete(data.id);
    if (generation) generation.resolve(data.text);
  } else if (data.type === 'error') {
    const generation = data.id !== undefined && generations.get(data.id);
    if (generation) {
      generations.delete(data.id);
      generation.reject(new Error(data.message));
    } else {
      modelProgressEl.hidden = true;
      modelLoading = false;
      modelFailed = true;
      modelSelectEl.disabled = busy;
      setStatus(modelDotEl, modelStatusEl, 'error', 'failed to load');
      rejectModelReady(new Error('Model failed to load: ' + data.message));
    }
  }
});

function loadModel(modelId) {
  const entry = MODELS.find(model => model.id === modelId) || MODELS[0];
  currentModel = entry;
  try { localStorage.setItem(MODEL_STORAGE_KEY, entry.id); } catch (err) { /* ignore */ }
  modelLoading = true;
  modelFailed = false;
  modelSelectEl.disabled = true;
  setStatus(modelDotEl, modelStatusEl, 'pending', 'loading…');
  modelReady = new Promise((resolve, reject) => {
    resolveModelReady = resolve;
    rejectModelReady = reject;
  });
  modelReady.catch(() => {});
  worker.postMessage({
    type: 'load',
    model: entry.id,
    templateOptions: entry.templateOptions,
    webgpuDtype: entry.webgpuDtype,
    modelOptions: entry.modelOptions,
    device: DEVICE_OVERRIDE,
    dtype: DTYPE_OVERRIDE,
  });
}

function generate(messages, tools, onToken, prefill) {
  const id = nextGenerationId++;
  return new Promise((resolve, reject) => {
    generations.set(id, { resolve, reject, onToken });
    worker.postMessage({ type: 'generate', id, messages, tools, prefill });
  });
}

function initModelSelect() {
  let saved = null;
  try { saved = localStorage.getItem(MODEL_STORAGE_KEY); } catch (err) { /* ignore */ }
  for (const model of MODELS) {
    const option = document.createElement('option');
    option.value = model.id;
    option.textContent = model.label;
    modelSelectEl.appendChild(option);
  }
  modelSelectEl.value = MODELS.some(model => model.id === saved) ? saved : MODELS[0].id;
  modelSelectEl.addEventListener('change', () => loadModel(modelSelectEl.value));
}

// ---- Chat --------------------------------------------------------------

let busy = false;

function renderMarkdown(el, text) {
  el.innerHTML = DOMPurify.sanitize(marked.parse(text));
}

function addMessage(text, className, usedTools, toolCalls) {
  const el = document.createElement('div');
  el.className = 'msg ' + className;

  if (className === 'bot') {
    renderMarkdown(el, text);

    const toolsEl = document.createElement('div');
    toolsEl.className = 'msg-tools';

    if (usedTools && usedTools.length > 0) {
      const counts = new Map();
      for (const name of usedTools) {
        counts.set(name, (counts.get(name) || 0) + 1);
      }

      const label = document.createElement('span');
      label.className = 'msg-tools-label';
      label.textContent = 'MCP tools used:';
      toolsEl.appendChild(label);

      for (const [name, count] of counts) {
        const badge = document.createElement('span');
        badge.className = 'tool-badge';
        const color = colorForTool(name);
        badge.style.backgroundColor = color.bg;
        badge.style.color = color.fg;
        badge.textContent = count > 1 ? `${name} ×${count}` : name;
        toolsEl.appendChild(badge);
      }

      if (toolCalls && toolCalls.length > 0) {
        const graphBtn = document.createElement('button');
        graphBtn.type = 'button';
        graphBtn.className = 'graph-btn';
        graphBtn.textContent = 'View data graph';
        graphBtn.addEventListener('click', () => showGraphFor(toolCalls));
        toolsEl.appendChild(graphBtn);
      }
    } else {
      toolsEl.classList.add('msg-tools-none');
      toolsEl.textContent = 'No MCP tool used';
    }

    el.appendChild(toolsEl);
  } else {
    el.textContent = text;
  }

  messagesEl.appendChild(el);
  messagesEl.scrollTop = messagesEl.scrollHeight;
  return el;
}

function setBusy(value) {
  busy = value;
  inputEl.disabled = value;
  modelSelectEl.disabled = value || modelLoading;
  submitEl.textContent = value ? 'Stop' : 'Send';
  submitEl.classList.toggle('stop', value);
}

formEl.addEventListener('submit', async (event) => {
  event.preventDefault();
  if (busy) {
    worker.postMessage({ type: 'interrupt' });
    return;
  }
  const message = inputEl.value.trim();
  if (!message) return;

  addMessage(message, 'user');
  inputEl.value = '';
  setBusy(true);

  const pending = addMessage('Thinking...', 'pending');
  // Live preview of the text being generated in the current step. Raw tool
  // call markup is hidden from it.
  const live = document.createElement('div');
  live.className = 'msg bot live';
  let liveText = '';

  try {
    if (!modelReady || modelFailed) loadModel(modelSelectEl.value);
    pending.textContent = 'Waiting for MCP server and local model…';
    await Promise.all([mcpReady, modelReady]);
    pending.textContent = 'Thinking...';

    const result = await runAgent({
      message,
      tools: mcpTools,
      system: systemPrompt(mcp.serverInfo && mcp.serverInfo.name, mcp.instructions),
      generate: (messages, tools, onToken, prefill) => {
        liveText = '';
        live.remove();
        return generate(messages, tools, onToken, prefill);
      },
      toolCallPrefill: currentModel.toolCallPrefill,
      callTool: (name, args) => mcp.callTool(name, args),
      onEvent: (event) => {
        if (event.type === 'token') {
          liveText += event.text;
          const shown = visibleText(liveText);
          if (shown) {
            if (!live.isConnected) messagesEl.insertBefore(live, pending);
            renderMarkdown(live, shown);
            messagesEl.scrollTop = messagesEl.scrollHeight;
          }
        } else if (event.type === 'tool-start') {
          const args = JSON.stringify(event.arguments);
          pending.textContent = `Calling ${event.name}${args === '{}' ? '' : ' ' + args}…`;
        } else if (event.type === 'tool-end') {
          pending.textContent = event.isError ? `${event.name} failed, continuing…` : 'Thinking...';
        }
      },
    });

    live.remove();
    pending.remove();
    addMessage(result.reply, 'bot', result.usedTools, result.toolCalls);
    if (result.toolCalls.length > 0) {
      renderGraph(result.toolCalls);
    }
  } catch (err) {
    live.remove();
    pending.remove();
    addMessage('Something went wrong: ' + err.message, 'error');
  } finally {
    setBusy(false);
    inputEl.focus();
  }
});

document.querySelectorAll('.suggestion').forEach((btn) => {
  btn.addEventListener('click', () => {
    if (busy) return;
    inputEl.value = btn.textContent;
    formEl.requestSubmit();
  });
});

// ---- Startup -----------------------------------------------------------

initGraph();
initModelSelect();
connectMcp();
loadModel(modelSelectEl.value);
