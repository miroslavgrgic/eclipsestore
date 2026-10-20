// Web Worker hosting the local chat model (Transformer.js), so that model
// download, compilation and token generation never block the UI thread.
//
// Messages in:  {type: 'load', model, templateOptions, webgpuDtype?, device?, dtype?}
//               {type: 'generate', id, messages, tools, prefill?, maxNewTokens}
//               {type: 'interrupt'}
// Messages out: {type: 'progress', loaded, total, file}
//               {type: 'ready', model, device, dtype}
//               {type: 'token', id, text}
//               {type: 'done', id, text}
//               {type: 'error', id?, message}

import {
  AutoTokenizer,
  AutoModelForCausalLM,
  TextStreamer,
  InterruptableStoppingCriteria,
  env,
} from 'https://cdn.jsdelivr.net/npm/@huggingface/transformers@4.3.1/dist/transformers.min.js';

// Models always come from the Hugging Face Hub (cached by the browser after
// the first download).
env.allowLocalModels = false;

let tokenizer = null;
let model = null;
let templateOptions = {};
let loaded = null; // {modelId, target} of the current model, for reloads
const stoppingCriteria = new InterruptableStoppingCriteria();

// The runtime returns the logits of every input position (n x 151936 floats
// for Qwen). On WebGPU, prompts of ~2000+ tokens outgrow the mappable buffer
// size and hang or kill the GPU device - so prompts are fed through the model
// in chunks first (see prefillCache), keeping the logits small.
const PREFILL_CHUNK_TOKENS = 512;
// Beyond that, the key/value cache gets big and generation slow.
const MAX_PROMPT_TOKENS = 6000;

// webgpuDtype is the model's preferred WebGPU weight format. q4f16 is not
// safe for every model: its fp16 activations break Qwen2.5 (the 1.5B model
// emits only "!!!!", the 0.5B one loops), hence the per-model setting.
async function pickDevice(webgpuDtype = 'q4') {
  if (self.navigator && navigator.gpu) {
    try {
      const adapter = await navigator.gpu.requestAdapter();
      if (adapter) {
        const f16 = adapter.features.has('shader-f16');
        return { device: 'webgpu', dtype: webgpuDtype.includes('f16') && !f16 ? 'q4' : webgpuDtype };
      }
    } catch (err) {
      // fall through to WASM
    }
  }
  return { device: 'wasm', dtype: 'q4' };
}

async function load(modelId, options, webgpuDtype, override) {
  if (model) {
    await model.dispose();
    model = null;
  }
  templateOptions = options || {};

  // Aggregate per-file download progress into one overall figure.
  const files = new Map();
  const progress_callback = event => {
    if (event.status !== 'progress' && event.status !== 'done') return;
    const entry = files.get(event.file) || { loaded: 0, total: 0 };
    if (event.total) entry.total = event.total;
    entry.loaded = event.status === 'done' ? entry.total : (event.loaded || 0);
    files.set(event.file, entry);
    let loaded = 0;
    let total = 0;
    for (const file of files.values()) {
      loaded += file.loaded;
      total += file.total;
    }
    self.postMessage({ type: 'progress', loaded, total, file: event.file });
  };

  tokenizer = await AutoTokenizer.from_pretrained(modelId, { progress_callback });
  let target = await pickDevice(webgpuDtype);
  if (override.device) target = { ...target, device: override.device };
  if (override.dtype) target = { ...target, dtype: override.dtype };
  try {
    model = await loadModel(modelId, target, progress_callback);
  } catch (err) {
    if (target.device !== 'webgpu') throw err;
    // WebGPU is available but unusable (driver / adapter quirks) - use WASM.
    console.warn('WebGPU failed, falling back to WASM:', err);
    target = { device: 'wasm', dtype: 'q4' };
    model = await loadModel(modelId, target, progress_callback);
  }
  loaded = { modelId, target };
  self.postMessage({ type: 'ready', model: modelId, ...target });
}

// After a WebGPU device error the session is unusable - every further run
// fails. Recreate it (the weights come from the browser cache).
async function recover() {
  if (!loaded || loaded.target.device !== 'webgpu') return;
  try {
    if (model) await model.dispose();
  } catch (err) {
    // the session is broken anyway
  }
  model = null;
  model = await loadModel(loaded.modelId, loaded.target);
}

async function loadModel(modelId, { device, dtype }, progress_callback) {
  const instance = await AutoModelForCausalLM.from_pretrained(modelId, { device, dtype, progress_callback });
  try {
    // Warm-up: compiles the WebGPU shaders now instead of on the first question.
    await instance.generate({ ...tokenizer('a'), max_new_tokens: 1 });
  } catch (err) {
    await instance.dispose();
    throw err;
  }
  return instance;
}

// Runs all but the last chunk of the prompt through the model, returning
// the resulting key/value cache (null for short prompts). generate() then
// only has to process the remaining tokens - same output, smaller logits.
async function prefillCache(inputs) {
  const length = inputs.input_ids.dims[1];
  let past = null;
  for (let end = PREFILL_CHUNK_TOKENS; end < length; end += PREFILL_CHUNK_TOKENS) {
    const result = await model.generate({
      input_ids: inputs.input_ids.slice(null, [0, end]),
      attention_mask: inputs.attention_mask.slice(null, [0, end]),
      past_key_values: past,
      max_new_tokens: 1,
      do_sample: false,
      return_dict_in_generate: true,
    });
    // generate() disposes the cache it was given; no need to do it here.
    past = result.past_key_values;
  }
  return past;
}

async function generate({ id, messages, tools, prefill, maxNewTokens }) {
  if (!model) throw new Error('Model is not loaded');
  // The prompt is rendered as text so that the reply can be prefilled, i.e.
  // forced to start with a given text (which is not part of the result).
  const prompt = tokenizer.apply_chat_template(messages, {
    ...(tools && tools.length > 0 && { tools }),
    add_generation_prompt: true,
    tokenize: false,
    ...templateOptions,
  }) + (prefill || '');
  const inputs = tokenizer(prompt, { add_special_tokens: false });
  const promptLength = inputs.input_ids.dims[1];
  console.debug(`generate #${id}: ${promptLength} prompt tokens`);
  if (promptLength > MAX_PROMPT_TOKENS) {
    throw new Error(`The prompt is too long for the local model (${promptLength} tokens, max ${MAX_PROMPT_TOKENS}). ` +
      'Try a narrower question, e.g. a shorter date range.');
  }

  stoppingCriteria.reset();
  const streamer = new TextStreamer(tokenizer, {
    skip_prompt: true,
    skip_special_tokens: true,
    callback_function: text => self.postMessage({ type: 'token', id, text }),
  });
  let output;
  try {
    const past_key_values = await prefillCache(inputs);
    output = await model.generate({
      ...inputs,
      past_key_values,
      max_new_tokens: maxNewTokens || 768,
      do_sample: false,
      streamer,
      stopping_criteria: stoppingCriteria,
    });
  } catch (err) {
    console.debug(`generate #${id} failed, recreating the session:`, err.message);
    await recover();
    console.debug('session recreated');
    throw err;
  }
  const [text] = tokenizer.batch_decode(output.slice(null, [promptLength, null]), { skip_special_tokens: true });
  self.postMessage({ type: 'done', id, text });
}

self.addEventListener('message', async ({ data }) => {
  try {
    if (data.type === 'load') {
      await load(data.model, data.templateOptions, data.webgpuDtype, { device: data.device, dtype: data.dtype });
    } else if (data.type === 'generate') {
      await generate(data);
    } else if (data.type === 'interrupt') {
      stoppingCriteria.interrupt();
    }
  } catch (err) {
    console.error(err);
    self.postMessage({ type: 'error', id: data.id, message: err.message || String(err) });
  }
});
