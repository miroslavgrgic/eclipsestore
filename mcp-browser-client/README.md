# MCP Browser Client

A browser-based MCP client that runs the language model **locally in the browser** with
[Transformer.js](https://huggingface.co/docs/transformers.js). It uses no external AI provider and
needs no API key. The UI matches `eclipsestore-client`: a tools rail, the chat, and the entity data graph.

```
Browser ──────────────────────────────────────────────┐
│ app.js       UI (chat, tools rail, data graph)      │
│ agent.js     tool-calling loop                      │
│ mcp.js       MCP client (Streamable HTTP, JSON-RPC) │──► /mcp ──► server.js ──► http://localhost:8080/mcp (sb-hotel)
│ llm-worker.js  Transformer.js model in a Web Worker │
└─────────────────────────────────────────────────────┘
```

## Run

```bash
# 1. start the MCP server (sb-hotel) on :8080
(cd sb-hotel && ./gradlew bootRun)

# 2. start the client (Node >= 20, no npm install needed)
cd mcp-browser-client
npm start                          # http://localhost:3000
npm run dev                        # same, but restarts when server.js changes
```

Files in `public/` are read from disk on every request, so a browser reload is enough to pick up
frontend changes, in both modes.

Environment variables for `server.js`:

| Variable  | Default                      |
|-----------|------------------------------|
| `PORT`    | `3000`                       |
| `MCP_URL` | `http://localhost:8080/mcp`  |

`server.js` serves `public/` and proxies `/mcp`, because sb-hotel sends no CORS headers. To use an
MCP server that does allow CORS, open `http://localhost:3000/?mcp=http://host:port/mcp`.

## How it works

1. **MCP**: on startup `mcp.js` sends `initialize` and `notifications/initialized`, then calls `tools/list`.
   It accepts plain JSON and SSE responses, and keeps `Mcp-Session-Id` when the server issues one.
2. **Model**: `llm-worker.js` loads the selected ONNX model from the Hugging Face Hub. The default,
   Qwen3 0.6B (`q4f16`), is a 570 MB download that the browser caches afterwards. It uses WebGPU when
   available, and falls back to WASM, which is much slower. Prompts longer than 512 tokens are run through
   the model in chunks first (*chunked prefill*). Otherwise the runtime would return logits for every
   prompt position, and on WebGPU that hangs or kills the GPU device from about 2,000 tokens. After a GPU
   device error, the worker recreates the session.
3. **Agent**: `agent.js` passes the MCP tools to the model through its chat template. When the model
   answers with `<tool_call>{…}</tool_call>`, the tool runs via `tools/call` and the result goes back
   to the model as a `tool` message. Tweaks for small models:
   - The first reply is forced to start with `<tool_call>`, because small models otherwise invent numbers.
   - A tool error asks the model to fix its arguments and call the tool again.
   - After a successful call, or when the model starts repeating a call, the tools are withheld so that
     the model has to answer from the results. This also makes the prompt ~1,500 tokens shorter.
   - The model sees at most 3,000 characters of each tool result. The data graph gets the full payload.
   - Every question is answered on its own: earlier questions and answers are not passed to the model,
     so a follow-up has to repeat what it refers to (e.g. a guest's name or id).

## Models

| Model / weights                  | Size         | Result in the browser (Chromium 153, WebGPU on Apple Metal)        |
|----------------------------------|--------------|--------------------------------------------------------------------|
| **Qwen3 0.6B, `q4f16`** (default) | 570 MB      | right tools and arguments; answers in 5 s to 40 s; weak at filtering |
| Qwen2.5 0.5B, `q4`               | 786 MB       | coherent, but often skips tool calls                               |
| Qwen2.5 0.5B, `q4f16`            | 483 MB       | degenerate, looping output                                         |
| Qwen2.5 1.5B, `q4f16`            | 1.2 GB       | output is only `!!!!` (fp16 overflow)                              |
| Qwen2.5 1.5B, `q4`               | 1.8 GB       | does not load: `std::bad_alloc`                                    |
| Qwen3 1.7B, `q4f16`              | 1.4 GB       | does not load: `std::bad_alloc` (also on Transformers.js 3.8.1)    |
| Llama 3.2 1B Instruct, `q4f16`   | 1.1 GB       | offered, untested                                                  |
| Llama 3.2 3B Instruct, `q4f16`   | 2.4 GB       | offered, untested (needs a strong GPU)                             |
| Qwen3 4B, `q4f16`                | 2.8 GB       | offered, untested (needs a strong GPU)                             |

Single-file weights of about 1.4 GB and more don't load in the browser, even on WebGPU. The
untested larger models ship their weights as external data files instead, the format Hugging Face's
WebGPU demos use for models of that size. Whether they load depends on the GPU memory. Llama models
emit tool calls as bare JSON (`{"name": …, "parameters": …}`) instead of `<tool_call>` tags. Each
model entry in `app.js` sets the matching `toolCallPrefill`. In Node, the
larger Qwen2.5 1.5B (`q4`) handles the tools well. Override the automatic choice with
`?device=webgpu|wasm` and `?dtype=q4|q4f16|q8|fp16`.

Small local models make mistakes. Check the "MCP tools used" badges and the data graph to see
what data an answer is really based on.

## Files

- `public/graph.js` is taken over from `eclipsestore-client/src/main/resources/static/app.js`. Keep both in sync.
- `public/styles.css` is the eclipsestore-client stylesheet, plus a section at the end for the status panel.
- `public/resize.js` is the same as in eclipsestore-client, with its own storage key.
