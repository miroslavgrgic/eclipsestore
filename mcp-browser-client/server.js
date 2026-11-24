// Zero-dependency dev server for the MCP browser client:
//   - serves ./public
//   - proxies /mcp to the MCP server, so the browser stays same-origin
//     (sb-hotel sends no CORS headers, so the browser can't call it directly)
//
// Usage: node server.js   (env: PORT=3000, MCP_URL=http://localhost:8080/mcp)

import http from 'node:http';
import { readFile } from 'node:fs/promises';
import { extname, join, normalize, sep } from 'node:path';
import { Readable } from 'node:stream';
import { fileURLToPath } from 'node:url';

const PORT = Number(process.env.PORT || 3000);
const MCP_URL = process.env.MCP_URL || 'http://localhost:8080/mcp';
const PUBLIC_DIR = fileURLToPath(new URL('./public/', import.meta.url));

const CONTENT_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.json': 'application/json',
};

// Request headers relevant to the MCP streamable HTTP transport.
const FORWARDED_REQUEST_HEADERS = ['content-type', 'accept', 'mcp-session-id', 'mcp-protocol-version', 'last-event-id'];
const FORWARDED_RESPONSE_HEADERS = ['content-type', 'mcp-session-id', 'cache-control'];

async function proxyMcp(req, res) {
  const headers = {};
  for (const name of FORWARDED_REQUEST_HEADERS) {
    if (req.headers[name]) headers[name] = req.headers[name];
  }
  const hasBody = req.method !== 'GET' && req.method !== 'HEAD';
  const controller = new AbortController();
  res.on('close', () => controller.abort());
  try {
    const upstream = await fetch(MCP_URL, {
      method: req.method,
      headers,
      body: hasBody ? Readable.toWeb(req) : undefined,
      duplex: hasBody ? 'half' : undefined,
      signal: controller.signal,
    });
    const responseHeaders = {};
    for (const name of FORWARDED_RESPONSE_HEADERS) {
      const value = upstream.headers.get(name);
      if (value) responseHeaders[name] = value;
    }
    res.writeHead(upstream.status, responseHeaders);
    if (upstream.body) {
      Readable.fromWeb(upstream.body).on('error', () => res.destroy()).pipe(res);
    } else {
      res.end();
    }
  } catch (err) {
    if (controller.signal.aborted) return;
    res.writeHead(502, { 'content-type': 'text/plain; charset=utf-8' });
    res.end(`MCP server ${MCP_URL} is not reachable: ${err.cause?.code || err.message}`);
  }
}

async function serveStatic(req, res) {
  const pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
  const filePath = normalize(join(PUBLIC_DIR, pathname === '/' ? 'index.html' : pathname));
  if (!filePath.startsWith(PUBLIC_DIR) && filePath + sep !== PUBLIC_DIR) {
    res.writeHead(403).end();
    return;
  }
  try {
    const content = await readFile(filePath);
    res.writeHead(200, {
      'content-type': CONTENT_TYPES[extname(filePath)] || 'application/octet-stream',
      'cache-control': 'no-cache',
    });
    res.end(content);
  } catch (err) {
    res.writeHead(404, { 'content-type': 'text/plain; charset=utf-8' }).end('Not found');
  }
}

http
  .createServer((req, res) => {
    const pathname = new URL(req.url, 'http://localhost').pathname;
    if (pathname === '/mcp') return proxyMcp(req, res);
    return serveStatic(req, res);
  })
  .listen(PORT, () => {
    console.log(`MCP browser client on http://localhost:${PORT}  (proxying /mcp -> ${MCP_URL})`);
  });
