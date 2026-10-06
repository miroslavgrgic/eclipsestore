// Drag handles on the rail / graph panel borders that resize the .app grid columns.
// Widths are kept in the --rail-w / --graph-w CSS variables and remembered per browser.
(function () {
  var app = document.querySelector('.app');
  var MIN_CHAT = 360;
  var RAIL_W_GRAPH_OPEN = 250; // matches .app:has(#graph-panel[open]) in styles.css
  var GRAPH_W_CLOSED = 48;
  var LIMITS = {
    rail: { prop: '--rail-w', min: 180, max: 480, initial: 272 },
    graph: { prop: '--graph-w', min: 280, max: 1000, initial: 420 }
  };
  var STORAGE_KEY = 'hotel-assistant.col-widths';

  function load() {
    try { return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {}; } catch (e) { return {}; }
  }
  function save(widths) {
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(widths)); } catch (e) { /* ignore */ }
  }

  var widths = load();

  function currentWidth(col) {
    return widths[col] || LIMITS[col].initial;
  }

  function setWidth(col, px) {
    // The rail is only resizable while the graph is closed; an open graph pins the rail at 250px.
    var otherWidth = col === 'rail' ? GRAPH_W_CLOSED : RAIL_W_GRAPH_OPEN;
    var max = Math.min(LIMITS[col].max, window.innerWidth - otherWidth - MIN_CHAT);
    px = Math.round(Math.max(LIMITS[col].min, Math.min(max, px)));
    widths[col] = px;
    app.style.setProperty(LIMITS[col].prop, px + 'px');
  }

  Object.keys(widths).forEach(function (col) {
    if (LIMITS[col]) setWidth(col, widths[col]);
  });

  document.querySelectorAll('.col-resizer').forEach(function (handle) {
    var col = handle.dataset.col;

    handle.addEventListener('pointerdown', function (e) {
      if (e.button !== 0) return;
      e.preventDefault();
      handle.setPointerCapture(e.pointerId);
      handle.classList.add('dragging');
      document.body.classList.add('resizing-cols');
    });

    handle.addEventListener('pointermove', function (e) {
      if (!handle.hasPointerCapture(e.pointerId)) return;
      var appRect = app.getBoundingClientRect();
      setWidth(col, col === 'rail' ? e.clientX - appRect.left : appRect.right - e.clientX);
    });

    function endDrag(e) {
      if (!handle.hasPointerCapture(e.pointerId)) return;
      handle.releasePointerCapture(e.pointerId);
      handle.classList.remove('dragging');
      document.body.classList.remove('resizing-cols');
      save(widths);
    }
    handle.addEventListener('pointerup', endDrag);
    handle.addEventListener('pointercancel', endDrag);

    // Double-click restores the default width.
    handle.addEventListener('dblclick', function () {
      setWidth(col, LIMITS[col].initial);
      save(widths);
    });

    // Arrow keys resize when the handle has focus.
    handle.addEventListener('keydown', function (e) {
      if (e.key !== 'ArrowLeft' && e.key !== 'ArrowRight') return;
      e.preventDefault();
      var step = e.shiftKey ? 64 : 16;
      var grow = (e.key === 'ArrowRight') === (col === 'rail');
      setWidth(col, currentWidth(col) + (grow ? step : -step));
      save(widths);
    });
  });
})();
