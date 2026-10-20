// Data graph of the entities found in an answer's MCP tool payloads.
// Taken over from eclipsestore-client (static/app.js) - keep both in sync.

const graphPanelEl = document.getElementById('graph-panel');
const graphPanelSummaryEl = graphPanelEl.querySelector('summary');
const graphLegendEl = document.getElementById('graph-legend');
const graphContainerEl = document.getElementById('graph-container');
const graphEmptyEl = document.getElementById('graph-empty');

// ---- Data graph -------------------------------------------------------
// Builds a node/edge graph out of the raw MCP tool call payloads (input +
// output JSON) received for a chat answer, by structurally recognizing
// known hotel entity shapes (guest, booking, room, payment) wherever they
// appear in the payload - rather than hard-coding per tool name - so any
// tool returning (or nesting) one of these shapes is visualized the same way.

const GROUP_STYLES = {
  guest:   { label: 'Guest',   dot: '#6366f1', color: { background: 'rgba(99, 102, 241, 0.85)', border: '#6366f1', highlight: { background: '#6366f1', border: '#4338ca' } }, shape: 'dot', size: 14 },
  booking: { label: 'Booking', dot: '#a855f7', color: { background: 'rgba(168, 85, 247, 0.85)', border: '#a855f7', highlight: { background: '#a855f7', border: '#7e22ce' } }, shape: 'box' },
  room:    { label: 'Room',    dot: '#0d9488', color: { background: 'rgba(20, 184, 166, 0.85)', border: '#0d9488', highlight: { background: '#0d9488', border: '#0f766e' } }, shape: 'dot', size: 14 },
  payment: { label: 'Payment', dot: '#db2777', color: { background: 'rgba(236, 72, 153, 0.85)', border: '#db2777', highlight: { background: '#db2777', border: '#9d174d' } }, shape: 'diamond', size: 14 },
};

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function isPlainObject(value) {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}

function shortId(id) {
  const text = String(id);
  return text.length > 8 ? text.slice(0, 8) + '…' : text;
}

function formatMoney(amount) {
  return amount === undefined || amount === null ? null : `€${amount}`;
}

// An MCP tool result isn't the tool's return value directly - it's a list of
// content blocks (Spring AI serializes it as e.g. [{"text": "<json>"}]), with
// the actual JSON payload embedded as a *string* inside the "text" field(s).
// Unwraps that envelope (if present) and parses the real payload out of it.
function unwrapToolOutput(raw) {
  let parsed;
  try {
    parsed = JSON.parse(raw);
  } catch (err) {
    return null;
  }
  const isContentBlockList = Array.isArray(parsed) && parsed.length > 0 &&
    parsed.every(item => isPlainObject(item) && typeof item.text === 'string');
  if (!isContentBlockList) return parsed;
  try {
    return JSON.parse(parsed.map(item => item.text).join(''));
  } catch (err) {
    return null;
  }
}

// Parses the JSON payloads of a message's MCP tool calls into a graph of
// entities (nodes) and relationships between them (edges).
function buildGraph(toolCalls) {
  const nodes = new Map();
  const edges = new Map();

  // `specific` marks a label backed by real entity data, as opposed to a
  // placeholder stub created just so a dangling reference (e.g. a payment's
  // bookingId with no accompanying booking object) has a node to point an
  // edge at. A later, specific sighting of the same entity upgrades the label;
  // a later stub sighting never downgrades one that's already specific.
  function upsertNode(id, group, label, details, specific) {
    const existing = nodes.get(id);
    if (existing) {
      existing.details = Object.assign({}, details, existing.details);
      if (specific && !existing.specific) {
        existing.label = label;
        existing.specific = true;
      }
      return existing;
    }
    const node = { id, group, label, details: details || {}, specific: !!specific };
    nodes.set(id, node);
    return node;
  }

  function addEdge(from, to, label) {
    if (!from || !to || from === to) return;
    const key = `${from}=>${to}:${label}`;
    if (!edges.has(key)) edges.set(key, { from, to, label });
  }

  function visitGuest(guest) {
    if (!guest || !guest.id) return null;
    const label = [guest.firstName, guest.lastName].filter(Boolean).join(' ') || `Guest ${shortId(guest.id)}`;
    upsertNode(`guest:${guest.id}`, 'guest', label, guest, true);
    return `guest:${guest.id}`;
  }

  function visitRoom(room, fallbackName) {
    if (isPlainObject(room) && room.id) {
      upsertNode(`room:${room.id}`, 'room', room.name || `Room ${shortId(room.id)}`, room, true);
      return `room:${room.id}`;
    }
    const name = isPlainObject(room) ? room.name : (room || fallbackName);
    if (!name) return null;
    const id = `room:name:${name}`;
    upsertNode(id, 'room', name, { name }, true);
    return id;
  }

  // Ensures a booking node exists for the given id, even when all we have is
  // the id itself (e.g. referenced only by a payment) - so edges always have
  // a real node to point at. Call visitBooking() instead when the full/summary
  // booking object is available, for a properly labeled, "specific" node.
  function ensureBookingStub(bookingId) {
    upsertNode(`booking:${bookingId}`, 'booking', `Booking ${shortId(bookingId)}`, { id: bookingId }, false);
  }

  function visitBooking(booking) {
    if (!booking || !booking.id) return null;
    const bookingKey = `booking:${booking.id}`;
    const roomLabel = booking.roomName || (isPlainObject(booking.room) ? booking.room.name : null);
    upsertNode(bookingKey, 'booking', roomLabel ? `Booking · ${roomLabel}` : `Booking ${shortId(booking.id)}`, booking, true);

    const roomKey = visitRoom(booking.room, booking.roomName);
    if (roomKey) addEdge(bookingKey, roomKey, 'room');

    if (Array.isArray(booking.guests)) {
      booking.guests.forEach(guest => {
        const guestKey = visitGuest(guest);
        if (guestKey) addEdge(guestKey, bookingKey, 'guest');
      });
    } else if (Array.isArray(booking.guestNames)) {
      booking.guestNames.forEach(name => {
        if (!name) return;
        const guestKey = `guest:name:${name}`;
        upsertNode(guestKey, 'guest', name, { firstName: name }, true);
        addEdge(guestKey, bookingKey, 'guest');
      });
    }
    return bookingKey;
  }

  function visitPayment(payment, mapKeyHint) {
    const bookingId = payment.bookingId || (isPlainObject(payment.booking) ? payment.booking.id : null);
    // Payment itself carries no id field (only the map it lives in does, via
    // mapKeyHint) - and this domain only ever has one payment per booking, so
    // the booking id is a stable fallback key when no map key was supplied.
    const paymentId = mapKeyHint || payment.id || (bookingId ? `for-${bookingId}` : null);
    if (!paymentId) return null;
    const paymentKey = `payment:${paymentId}`;
    upsertNode(paymentKey, 'payment', (mapKeyHint || payment.id) ? `Payment ${shortId(paymentId)}` : 'Payment', payment, true);

    if (bookingId) {
      if (isPlainObject(payment.booking)) {
        visitBooking(payment.booking);
      } else {
        ensureBookingStub(bookingId);
      }
      addEdge(paymentKey, `booking:${bookingId}`, 'payment');
    }
    return paymentKey;
  }

  // Recognizes a known entity shape by its fields (duck typing) wherever
  // it occurs in the payload, and recurses into anything else (arrays,
  // UUID-keyed maps, DTO wrapper objects) looking for nested entities.
  function visit(value, mapKeyHint, depth) {
    if (value == null || depth > 8) return;

    if (Array.isArray(value)) {
      value.forEach(item => visit(item, null, depth + 1));
      return;
    }
    if (!isPlainObject(value)) return;

    if (isPlainObject(value.guest) && 'score' in value) {
      visit(value.guest, null, depth + 1);
      return;
    }
    if ('firstName' in value && 'lastName' in value && 'id' in value) {
      visitGuest(value);
      return;
    }
    if ('id' in value && 'from' in value && 'to' in value) {
      visitBooking(value);
      return;
    }
    if ('name' in value && 'id' in value && ('sqm' in value || 'defaultPrice' in value)) {
      visitRoom(value);
      return;
    }
    if ('paymentProviderId' in value) {
      visitPayment(value, mapKeyHint);
      return;
    }

    for (const [key, nested] of Object.entries(value)) {
      if (isPlainObject(nested) || Array.isArray(nested)) {
        visit(nested, UUID_RE.test(key) ? key : null, depth + 1);
      }
    }
  }

  toolCalls.forEach(toolCall => {
    const output = unwrapToolOutput(toolCall.output);
    if (output == null) return;
    visit(output, null, 0);

    // findSimilarGuests relates its *input* guestId to the guests in its
    // output - a relationship that only exists by joining the two.
    if (toolCall.name === 'findSimilarGuests') {
      try {
        const input = JSON.parse(toolCall.input);
        const sourceKey = input.guestId ? `guest:${input.guestId}` : null;
        if (sourceKey && Array.isArray(output) && output.length > 0) {
          // The source guest's profile isn't part of this tool's own
          // output - ensure it still gets a node so the edge below has
          // somewhere to point. upsertNode() will upgrade this stub's
          // label automatically if a fuller profile for the same id
          // surfaces elsewhere in the same answer.
          upsertNode(sourceKey, 'guest', `Guest ${shortId(input.guestId)}`, { id: input.guestId }, false);
          output.forEach(match => {
            if (isPlainObject(match.guest) && match.guest.id) {
              addEdge(sourceKey, `guest:${match.guest.id}`, `similar ${Math.round(match.score * 100)}%`);
            }
          });
        }
      } catch (err) {
        // ignore malformed input payload
      }
    }
  });

  // Reconciles name-only guest placeholders (created where a payload only
  // gave us a guest's name, e.g. BookingSummary.guestNames) with a fully
  // resolved guest node of the same name seen elsewhere in this answer.
  const idByName = new Map();
  for (const node of nodes.values()) {
    if (node.group === 'guest' && !node.id.startsWith('guest:name:')) {
      idByName.set(node.label, node.id);
    }
  }
  for (const node of Array.from(nodes.values())) {
    if (node.group !== 'guest' || !node.id.startsWith('guest:name:')) continue;
    const realId = idByName.get(node.label);
    if (!realId || realId === node.id) continue;
    edges.forEach(edge => {
      if (edge.from === node.id) edge.from = realId;
      if (edge.to === node.id) edge.to = realId;
    });
    nodes.delete(node.id);
  }

  return { nodes: Array.from(nodes.values()), edges: Array.from(edges.values()) };
}

function nodeTooltip(node) {
  const lines = [node.label];
  const d = node.details || {};
  if (node.group === 'guest' && d.age !== undefined) {
      lines.push(`Age: ${d.age}`);
      lines.push(`Id: ${d.id}`);
  }
  if (node.group === 'booking') {
    if (d.from && d.to) lines.push(`${d.from} → ${d.to}`);
    if (d.price !== undefined) lines.push(`Price: ${formatMoney(d.price)}`);
    if (d.paymentStatus) lines.push(`Status: ${d.paymentStatus}`);
    lines.push(`Id: ${d.id}`);
  }
  if (node.group === 'payment') {
    if (d.price !== undefined) lines.push(`Price: ${formatMoney(d.price)}`);
    if (d.paymentProviderId) lines.push(`Provider: ${d.paymentProviderId}`);
  }
  if (node.group === 'room' && d.defaultPrice !== undefined) lines.push(`Price: ${formatMoney(d.defaultPrice)}`);
  return lines.join('\n');
}

let graphNetwork = null;

export function renderGraphLegend() {
  graphLegendEl.innerHTML = '';
  for (const style of Object.values(GROUP_STYLES)) {
    const item = document.createElement('div');
    item.className = 'legend-item';
    const dot = document.createElement('span');
    dot.className = 'legend-dot';
    dot.style.background = style.dot;
    item.appendChild(dot);
    const label = document.createElement('span');
    label.textContent = style.label;
    item.appendChild(label);
    graphLegendEl.appendChild(item);
  }
}

export function renderGraph(toolCalls) {
  const { nodes, edges } = buildGraph(toolCalls || []);
  graphPanelSummaryEl.textContent = `Data graph (${nodes.length} ${nodes.length === 1 ? 'entity' : 'entities'})`;

  if (graphNetwork) {
    graphNetwork.destroy();
    graphNetwork = null;
  }
  graphContainerEl.innerHTML = '';

  if (nodes.length === 0) {
    graphContainerEl.style.display = 'none';
    graphEmptyEl.style.display = 'block';
    graphEmptyEl.textContent = 'No guest/booking/room/payment relationships found in this answer\'s MCP payload.';
    return;
  }
  graphContainerEl.style.display = 'block';
  graphEmptyEl.style.display = 'none';

  const visNodes = nodes.map(node => {
    const { label, dot, ...style } = GROUP_STYLES[node.group];
    return { id: node.id, label: node.label, title: nodeTooltip(node), ...style };
  });
  const visEdges = edges.map(edge => ({
    from: edge.from,
    to: edge.to,
    label: edge.label === 'guest' || edge.label === 'room' || edge.label === 'payment' ? '' : edge.label,
    arrows: 'to',
    color: { color: 'rgba(130, 130, 140, 0.55)', highlight: '#7c3aed' },
    font: { size: 10, color: '#9a94a6', strokeWidth: 0 },
    smooth: { type: 'continuous' },
  }));

  // Large result sets (e.g. "list all payments") can produce hundreds of
  // nodes - cap stabilization effort accordingly, and freeze the layout
  // once stable so the graph doesn't keep redrawing/dragging on the CPU.
  const stabilizationIterations = Math.min(150, Math.max(50, Math.round(6000 / Math.max(visNodes.length, 1))));
  graphNetwork = new vis.Network(graphContainerEl, { nodes: visNodes, edges: visEdges }, {
    autoResize: true,
    interaction: { hover: true, tooltipDelay: 120 },
    physics: { solver: 'forceAtlas2Based', forceAtlas2Based: { springLength: 120, avoidOverlap: 0.6 }, stabilization: { iterations: stabilizationIterations } },
    nodes: { font: { color: '#1d1d1f', size: 12, face: 'Barlow, sans-serif' }, borderWidth: 2 },
  });
  graphNetwork.once('stabilizationIterationsDone', () => {
    if (!graphNetwork) return;
    graphNetwork.setOptions({ physics: false });
    graphNetwork.fit({ animation: false });
  });
}

// The network may be built while the panel is collapsed (zero-size
// container), which leaves the viewport off-center - re-fit once visible.
graphPanelEl.addEventListener('toggle', () => {
  if (!graphPanelEl.open || !graphNetwork) return;
  requestAnimationFrame(() => {
    if (!graphNetwork) return;
    graphNetwork.redraw();
    graphNetwork.fit({ animation: false });
  });
});

export function showGraphFor(toolCalls) {
  graphPanelEl.open = true;
  renderGraph(toolCalls);
  graphPanelEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

export function initGraph() {
  renderGraphLegend();
  graphContainerEl.style.display = 'none';
  graphEmptyEl.textContent = 'Ask a question to populate the data graph.';
}
