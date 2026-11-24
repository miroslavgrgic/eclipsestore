// Minimal MCP client for the "Streamable HTTP" transport: every JSON-RPC
// message is POSTed to a single endpoint, and the server answers either with
// a plain JSON body or with an SSE stream carrying the response. Works with
// both stateless servers (like sb-hotel) and session-based ones (the
// Mcp-Session-Id header is captured and replayed when the server issues one).
// Free of DOM dependencies, so it also runs under Node for testing.

const PROTOCOL_VERSION = '2025-06-18';

export class McpError extends Error {
  constructor(message, code, data) {
    super(message);
    this.name = 'McpError';
    this.code = code;
    this.data = data;
  }
}

export class McpClient {
  constructor(url, clientInfo = { name: 'mcp-browser-client', version: '0.1.0' }) {
    this.url = url;
    this.clientInfo = clientInfo;
    this.sessionId = null;
    this.protocolVersion = null;
    this.serverInfo = null;
    this.instructions = null;
    this.nextId = 1;
  }

  async connect() {
    const result = await this.request('initialize', {
      protocolVersion: PROTOCOL_VERSION,
      capabilities: {},
      clientInfo: this.clientInfo,
    });
    this.protocolVersion = result.protocolVersion;
    this.serverInfo = result.serverInfo || null;
    this.instructions = result.instructions || null;
    await this.notify('notifications/initialized');
    return result;
  }

  async listTools() {
    const tools = [];
    let cursor;
    do {
      const result = await this.request('tools/list', cursor ? { cursor } : undefined);
      tools.push(...(result.tools || []));
      cursor = result.nextCursor;
    } while (cursor);
    return tools;
  }

  callTool(name, args) {
    return this.request('tools/call', { name, arguments: args || {} });
  }

  async request(method, params) {
    const id = this.nextId++;
    const response = await this.post({ jsonrpc: '2.0', id, method, ...(params !== undefined && { params }) });
    const message = await readResponse(response, id);
    if (message.error) {
      throw new McpError(message.error.message || 'MCP error', message.error.code, message.error.data);
    }
    return message.result;
  }

  async notify(method, params) {
    const response = await this.post({ jsonrpc: '2.0', method, ...(params !== undefined && { params }) });
    // Notifications are acknowledged with 202 Accepted and no body.
    await response.body?.cancel();
  }

  async post(body) {
    const headers = {
      'Content-Type': 'application/json',
      Accept: 'application/json, text/event-stream',
    };
    if (this.sessionId) headers['Mcp-Session-Id'] = this.sessionId;
    if (this.protocolVersion) headers['MCP-Protocol-Version'] = this.protocolVersion;

    const response = await fetch(this.url, { method: 'POST', headers, body: JSON.stringify(body) });
    if (!response.ok) {
      const detail = (await response.text()).trim();
      throw new McpError(`MCP server responded with HTTP ${response.status}${detail ? ': ' + detail : ''}`);
    }
    const sessionId = response.headers.get('Mcp-Session-Id');
    if (sessionId) this.sessionId = sessionId;
    return response;
  }
}

async function readResponse(response, id) {
  const contentType = response.headers.get('Content-Type') || '';
  if (!contentType.includes('text/event-stream')) {
    return response.json();
  }

  // SSE: events are separated by a blank line; a JSON-RPC message is carried
  // in the event's data field(s). Skip server-initiated messages until the
  // response matching our request id arrives.
  const reader = response.body.pipeThrough(new TextDecoderStream()).getReader();
  let buffer = '';
  try {
    for (;;) {
      const { value, done } = await reader.read();
      if (done) break;
      buffer += value.replace(/\r\n/g, '\n');
      let boundary;
      while ((boundary = buffer.indexOf('\n\n')) !== -1) {
        const event = buffer.slice(0, boundary);
        buffer = buffer.slice(boundary + 2);
        const data = event
          .split('\n')
          .filter(line => line.startsWith('data:'))
          .map(line => line.slice(5).replace(/^ /, ''))
          .join('\n');
        if (!data) continue;
        const message = JSON.parse(data);
        if (message.id === id) return message;
      }
    }
  } finally {
    reader.cancel().catch(() => {});
  }
  throw new McpError('MCP stream ended without a response');
}

// Joins the text content blocks of a tools/call result.
export function toolResultText(result) {
  const blocks = (result && result.content) || [];
  return blocks
    .map(block => (block.type === 'text' ? block.text : `[${block.type} content]`))
    .join('\n');
}
