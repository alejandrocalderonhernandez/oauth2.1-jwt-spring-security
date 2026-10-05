// ─────────────────────────────────────────────────────────────
//  Panel lateral didáctico. Tres pestañas:
//    Flujo       → en qué fase del diagrama OAuth va la app, con datos reales
//    Peticiones  → cada llamada a la API (headers, status, respuesta)
//    Token       → el access token por dentro (si es un JWT)
// ─────────────────────────────────────────────────────────────
import { CONFIG, stepByStep } from './config.js';
import { PHASES, getFlow, onFlowChange, resetFlow } from './flow.js';
import { requests, onRequests } from './api.js';
import { getTokens, refresh } from './oauth.js';
import { getSession } from './session.js';
import { icon } from './icons.js';

const esc = (s) =>
  String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

const STATUS_LABEL = {
  pending: 'pendiente',
  active: 'en curso',
  done: 'listo',
  error: 'error',
  server: 'en el servidor',
  skipped: 'omitida',
};

let tab = sessionStorage.getItem('orion.tab') || 'flow';
let body;

// ── Pestaña: Flujo ──────────────────────────────────────────

const MODES = [
  ['mock', 'Mock', 'Sin backend: datos de ejemplo y usuario simulado'],
  ['open', 'Abierto', 'Llama a tu API real, sin login ni token'],
  ['secure', 'Seguro', 'Login completo con OAuth 2.1 y PKCE'],
];

function renderFlow() {
  const flow = getFlow();
  let html = `<div class="modes"><span class="lbl">Modo</span><div class="seg" role="group" aria-label="Modo de la app">
    ${MODES.map(([id, label, tip]) => `<button data-mode="${id}" title="${esc(tip)}" class="${CONFIG.mode === id ? 'on' : ''}">${label}</button>`).join('')}
  </div></div>`;

  if (CONFIG.mode !== 'secure') {
    html += `<div class="note">${icon('info', 16)}<div>
      ${
        CONFIG.mode === 'mock'
          ? '<b>Modo mock.</b> No hay login real: el selector de arriba simula a un Visitante, un User o un Admin.'
          : '<b>Modo abierto.</b> La app llama a la API sin token. Sirve para mostrar las rutas antes de activar la seguridad.'
      }
      Para ver el flujo OAuth completo cambia a <b>Seguro</b>.</div></div>`;
  }

  if (CONFIG.mode === 'secure') {
    const t = getTokens();
    html += `<div class="controls">
      <label class="switch"><input type="checkbox" id="stepSwitch" ${stepByStep.enabled ? 'checked' : ''}>
        <span>Modo paso a paso</span></label>
      <div class="ctl-row">
        <button class="btn small ghost" data-act="reset">${icon('refresh-cw', 14)} Reiniciar bitácora</button>
        ${t?.refresh_token ? `<button class="btn small" data-act="refresh">${icon('key-round', 14)} Renovar token (fase 7)</button>` : ''}
      </div></div>`;
  }

  html += '<ol class="phases">';
  for (const p of PHASES) {
    const s = flow[p.id];
    const status = s?.status || 'pending';
    html += `<li class="phase s-${status}">
      <span class="dot">${p.id}</span>
      <div class="ph-body">
        <div class="ph-title"><b>${esc(p.title)}</b><span class="tag t-${status}">${STATUS_LABEL[status]}</span></div>
        <small>${esc(p.hint)}</small>
        ${
          s?.details?.length
            ? `<dl class="kv small">${s.details.map(([k, v]) => `<dt>${esc(k)}</dt><dd>${esc(v)}</dd>`).join('')}</dl>`
            : ''
        }
      </div></li>`;
  }
  html += '</ol>';
  return html;
}

// ── Pestaña: Peticiones ─────────────────────────────────────

const json = (v) => (v === undefined || v === null || v === '' ? '(vacío)' : typeof v === 'string' ? v : JSON.stringify(v, null, 2));

function renderRequests() {
  if (!requests.length) {
    return `<div class="empty">${icon('radio', 28)}<p>Aún no hay peticiones.<br>Abre <b>Noticias</b> o <b>Misiones</b> y aparecerán aquí.</p></div>`;
  }
  return `<ul class="reqs">${requests
    .map((r) => {
      const cls = r.networkError ? 'err' : r.status >= 500 ? 'err' : r.status >= 400 ? 'warn' : 'ok';
      return `<li><details>
        <summary><span class="m m-${r.method}">${r.method}</span><span class="p">${esc(r.path)}</span>
          <span class="st ${cls}">${r.networkError ? 'sin respuesta' : r.status}</span><span class="ms">${r.ms} ms</span></summary>
        <div class="rq">
          <h5>Petición</h5>
          <pre>${esc(r.method)} ${esc(r.url)}\n${Object.entries(r.reqHeaders)
            .map(([k, v]) => `${esc(k)}: ${esc(v)}`)
            .join('\n')}${r.reqBody !== undefined ? '\n\n' + esc(json(r.reqBody)) : ''}</pre>
          <h5>Respuesta ${r.networkError ? '' : r.status}</h5>
          <pre>${esc(json(r.resBody))}</pre>
        </div>
      </details></li>`;
    })
    .join('')}</ul>`;
}

// ── Pestaña: Token ──────────────────────────────────────────

function renderToken() {
  const session = getSession();
  if (CONFIG.mode !== 'secure') {
    return `<div class="empty">${icon('key-round', 28)}<p>Los tokens aparecen aquí en modo <code>?mode=secure</code>, después de iniciar sesión.</p></div>`;
  }
  const t = getTokens();
  if (!t) {
    return `<div class="empty">${icon('key-round', 28)}<p>Todavía no hay token.<br>Inicia sesión para ver qué contiene.</p></div>`;
  }
  const claims = session?.jwt?.payload;
  const exp = t.expires_at;
  return `
    <dl class="kv">
      <dt>token_type</dt><dd>${esc(t.token_type)}</dd>
      <dt>scopes concedidos</dt><dd>${t.scope ? t.scope.split(/\s+/).map((s) => `<span class="chip">${esc(s)}</span>`).join(' ') : '—'}</dd>
      <dt>caduca en</dt><dd><b id="countdown" data-exp="${exp}"></b></dd>
      <dt>refresh token</dt><dd>${t.refresh_token ? 'sí (llave de repuesto guardada)' : 'no'}</dd>
    </dl>
    <h5>Access token por dentro</h5>
    ${
      claims
        ? `<p class="hint">Es un JWT: tiene tres partes (header · payload · firma). Esto es el payload, ya decodificado:</p>
           <pre>${esc(JSON.stringify(claims, null, 2))}</pre>
           <p class="hint">${icon('shield-alert', 14)} Que la app pueda leerlo no significa que decida permisos con esto: la verificación de verdad la hace siempre el resource server.</p>`
        : `<p class="hint">Es un token opaco: la app no puede leer su contenido. Solo el servidor sabe qué significa.</p>
           <pre>${esc(t.access_token)}</pre>`
    }`;
}

// ── Montaje ─────────────────────────────────────────────────

function render() {
  if (!body) return;
  const views = { flow: renderFlow, requests: renderRequests, token: renderToken };
  body.innerHTML = views[tab]();
  document.querySelectorAll('.tabs button').forEach((b) => b.classList.toggle('on', b.dataset.tab === tab));
  tick();
}

function tick() {
  const el = document.getElementById('countdown');
  if (!el) return;
  const left = Math.round((Number(el.dataset.exp) - Date.now()) / 1000);
  el.textContent = left > 0 ? `${Math.floor(left / 60)} min ${left % 60} s` : 'caducó';
  el.classList.toggle('expired', left <= 0);
}

export function initPanel() {
  const panel = document.getElementById('panel');
  body = document.getElementById('panelBody');

  panel.querySelector('.tabs').addEventListener('click', (e) => {
    const b = e.target.closest('button[data-tab]');
    if (!b) return;
    tab = b.dataset.tab;
    sessionStorage.setItem('orion.tab', tab);
    render();
  });

  body.addEventListener('click', async (e) => {
    const modeBtn = e.target.closest('[data-mode]');
    if (modeBtn && modeBtn.dataset.mode !== CONFIG.mode) {
      sessionStorage.setItem('orion.mode', modeBtn.dataset.mode);
      // Quitamos ?mode= de la URL (si estaba) y recargamos ya en el nuevo modo.
      window.history.replaceState(null, '', window.location.pathname + window.location.hash);
      window.location.reload();
      return;
    }
    const act = e.target.closest('[data-act]')?.dataset.act;
    if (act === 'reset') resetFlow();
    if (act === 'refresh') {
      try {
        await refresh();
      } catch {
        /* el error queda en la bitácora de la fase 7 */
      }
    }
  });
  body.addEventListener('change', (e) => {
    if (e.target.id === 'stepSwitch') stepByStep.enabled = e.target.checked;
  });

  onFlowChange(render);
  onRequests(render);
  window.addEventListener('orion:auth', render);
  setInterval(tick, 1000);
  render();

  // Mostrar u ocultar el panel
  const layout = document.querySelector('.layout');
  const saved = sessionStorage.getItem('orion.panel');
  const open = saved ? saved === 'open' : window.innerWidth >= 1200;
  layout.classList.toggle('panel-open', open);
  document.getElementById('panelToggle').addEventListener('click', () => {
    const now = layout.classList.toggle('panel-open');
    sessionStorage.setItem('orion.panel', now ? 'open' : 'closed');
  });
  panel.querySelector('[data-act="close"]').addEventListener('click', () => {
    layout.classList.remove('panel-open');
    sessionStorage.setItem('orion.panel', 'closed');
  });
}

export function showTab(name) {
  tab = name;
  sessionStorage.setItem('orion.tab', tab);
  render();
}
