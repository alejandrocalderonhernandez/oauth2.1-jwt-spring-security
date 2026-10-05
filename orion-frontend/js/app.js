// ─────────────────────────────────────────────────────────────
//  Orión · Control de misión — pantallas y navegación
//
//    #/news          Noticias        (público)
//    #/missions      Misiones        (requiere mission.read)
//    #/missions/new  Nueva misión    (requiere mission.write)
// ─────────────────────────────────────────────────────────────
import { CONFIG } from './config.js';
import { hydrateIcons, icon } from './icons.js';
import { api } from './api.js';
import { login, logout, handleCallback } from './oauth.js';
import { getSession, can } from './session.js';
import { MOCK_ROLES, getMockRole, setMockRole } from './mock.js';
import { initPanel, showTab } from './panel.js';
import { initSpace } from './space.js';

const $ = (sel) => document.querySelector(sel);
const view = $('#view');
const esc = (s) =>
  String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

// ── Barra superior ──────────────────────────────────────────

const MODE_LABEL = {
  mock: ['MODO MOCK', 'mock'],
  open: ['SIN SEGURIDAD', 'open'],
  secure: ['OAuth 2.1 · PKCE', 'secure'],
};

function renderTopbar() {
  const [label, cls] = MODE_LABEL[CONFIG.mode] || MODE_LABEL.mock;
  const badge = $('#modeBadge');
  badge.textContent = label;
  badge.className = `mode m-${cls}`;

  const area = $('#userArea');
  const s = getSession();

  if (CONFIG.mode === 'mock') {
    area.innerHTML = `<label class="mock-sel"><span>Simular:</span>${icon('user-round', 16)}
      <select id="mockSelect" aria-label="Simular usuario">
        ${MOCK_ROLES.map(
          (r) =>
            `<option value="${r}" ${getMockRole() === r ? 'selected' : ''}>${{ visitor: 'Visitante', user: 'User', admin: 'Admin' }[r]}</option>`
        ).join('')}
      </select></label>`;
  } else if (CONFIG.mode === 'secure') {
    area.innerHTML = s
      ? `<div class="chip-user">${icon('user-round', 16)}<b>${esc(s.name)}</b>
           ${(s.roles.length ? s.roles : ['SIN ROL']).map((r) => `<em class="role">${esc(r)}</em>`).join('')}
           <button class="iconbtn small" id="logoutBtn" title="Cerrar sesión" aria-label="Cerrar sesión">${icon('log-out', 16)}</button>
         </div>`
      : `<button class="btn primary" id="loginBtn">${icon('log-in', 16)} Iniciar sesión</button>`;
  } else {
    area.innerHTML = '';
  }

  // Marca en la navegación lo que tu usuario puede abrir
  document.querySelectorAll('#nav .req').forEach((em) => {
    const needsWrite = em.textContent === 'ADMIN';
    const ok = CONFIG.mode === 'open' || can(s, needsWrite ? 'mission.write' : 'mission.read');
    em.classList.toggle('ok', ok);
  });
}

document.addEventListener('click', (e) => {
  if (e.target.closest('#loginBtn, [data-act="login"]')) startLogin();
  if (e.target.closest('#logoutBtn')) {
    logout();
    renderTopbar();
    route();
  }
});
document.addEventListener('change', (e) => {
  if (e.target.id === 'mockSelect') {
    setMockRole(e.target.value);
    renderTopbar();
    route();
  }
});
window.addEventListener('orion:auth', renderTopbar);

function startLogin() {
  sessionStorage.setItem('orion.return', window.location.hash || '#/missions');
  login().catch((err) => banner('error', err.message));
}

// ── Avisos ──────────────────────────────────────────────────

function banner(type, text) {
  $('#banner').innerHTML = text
    ? `<div class="banner ${type}">${icon(type === 'ok' ? 'circle-check' : 'triangle-alert', 18)}<span>${esc(text)}</span>
        <button class="iconbtn small" aria-label="Cerrar" onclick="this.parentElement.remove()">${icon('x', 14)}</button></div>`
    : '';
}

const loading = (text) => `<div class="loading">${icon('loader-circle', 22)}<span>${esc(text)}</span></div>`;

// Explica en palabras simples qué significa cada error HTTP.
function errorCard(r, needed) {
  let title, text, cls = 'warn', ic = 'triangle-alert';
  if (r.networkError) {
    title = 'No pude conectar con el servidor';
    text = `La petición a ${r.url} no tuvo respuesta. Revisa que el resource server esté corriendo y que permita CORS para ${window.location.origin}.`;
    cls = 'err';
    ic = 'ban';
  } else if (r.status === 401) {
    title = '401 · Sin autenticar';
    text = 'El servidor no recibió un token válido (falta o caducó). No sabe quién eres.';
    ic = 'lock-keyhole';
  } else if (r.status === 403) {
    title = '403 · Sin permiso';
    text = `Tu sesión es válida, pero tu usuario no tiene el permiso ${needed}. El servidor sabe quién eres y aun así dice que no.`;
    ic = 'shield-alert';
  } else {
    title = `Error ${r.status}`;
    text = 'El servidor respondió con un error inesperado.';
    cls = 'err';
  }
  return `<div class="errcard ${cls}">
    <div class="ec-head">${icon(ic, 22)}<h3>${esc(title)}</h3></div>
    <p>${esc(text)}</p>
    <div class="ec-actions">
      ${r.status === 401 && CONFIG.mode === 'secure' ? `<button class="btn primary" data-act="login">${icon('log-in', 16)} Iniciar sesión</button>` : ''}
      <button class="btn ghost" data-act="see-req">${icon('terminal', 16)} Ver la petición</button>
    </div>
  </div>`;
}

document.addEventListener('click', (e) => {
  if (e.target.closest('[data-act="see-req"]')) {
    document.querySelector('.layout').classList.add('panel-open');
    sessionStorage.setItem('orion.panel', 'open');
    showTab('requests');
  }
});

// ── Pantalla: Noticias (pública) ────────────────────────────

async function renderNews() {
  view.innerHTML = `${heading('newspaper', 'Noticias de Orión', 'Información pública de la base espacial. Cualquiera puede leerla, sin iniciar sesión.', 'GET /api/news', 'PÚBLICO')}${loading('Cargando noticias…')}`;
  const r = await api('GET', '/api/news', { withToken: false });
  if (!r.ok) return (view.innerHTML = heading('newspaper', 'Noticias de Orión', '', 'GET /api/news', 'PÚBLICO') + errorCard(r, ''));
  view.innerHTML =
    heading('newspaper', 'Noticias de Orión', 'Información pública de la base espacial. Cualquiera puede leerla, sin iniciar sesión.', 'GET /api/news', 'PÚBLICO') +
    `<div class="news-grid">${r.resBody
      .map(
        (n) => `<article class="card news">
          <div class="meta"><span class="tag-news">${esc(n.tag)}</span><time>${esc(n.date)}</time></div>
          <h3>${esc(n.headline)}</h3>
          <p>${esc(n.summary)}</p>
        </article>`
      )
      .join('')}</div>`;
}

// ── Pantalla: Misiones (requiere login) ─────────────────────

function lockScreen(kind = 'missions') {
  const secure = CONFIG.mode === 'secure';
  const isNew = kind === 'new';
  view.innerHTML =
    (isNew
      ? heading('file-plus-2', 'Proponer una misión', 'Formulario para registrar una nueva misión ante el centro de control.', 'POST /api/missions', 'mission.write')
      : heading('rocket', 'Misiones', 'Expedientes de las misiones en curso.', 'GET /api/missions', 'mission.read')) +
    `<div class="lock">
      <div class="lock-ic">${icon('lock-keyhole', 44)}</div>
      <h2>Contenido clasificado</h2>
      <p>${
        secure
          ? `${isNew ? 'Para proponer una misión' : 'Para ver los expedientes'} necesitas iniciar sesión${isNew ? ' (y tener permiso de admin)' : ''}. La app te llevará al servidor de login: tu contraseña nunca pasa por aquí.`
          : `Estás como visitante. Elige <b>${isNew ? 'Admin' : 'User</b> o <b>Admin'}</b> en el simulador de arriba para fingir un inicio de sesión.`
      }</p>
      <div class="lock-actions">
        ${secure ? `<button class="btn primary" data-act="login">${icon('log-in', 16)} Iniciar sesión</button>` : ''}
        ${isNew ? '' : `<button class="btn ghost" id="tryNoToken">${icon('eye', 16)} Probar sin token</button>`}
      </div>
      <div id="tryResult"></div>
    </div>`;
  if (isNew) return;
  $('#tryNoToken').addEventListener('click', async () => {
    $('#tryResult').innerHTML = loading('Pidiendo los expedientes sin token…');
    const r = await api('GET', '/api/missions', { withToken: false });
    $('#tryResult').innerHTML = r.ok
      ? `<div class="errcard warn"><p>El servidor respondió ${r.status}: la API está abierta (modo sin seguridad).</p></div>`
      : errorCard(r, 'mission.read');
  });
}

async function renderMissions() {
  const s = getSession();
  if (CONFIG.mode !== 'open' && !s) return lockScreen();

  const head = heading('rocket', 'Misiones', 'Expedientes de las misiones en curso. Solo para usuarios con sesión.', 'GET /api/missions', 'mission.read');
  view.innerHTML = head + loading('Abriendo expedientes…');
  const r = await api('GET', '/api/missions');
  if (!r.ok) return (view.innerHTML = head + errorCard(r, 'mission.read'));

  view.innerHTML =
    head +
    `<div class="missions">${r.resBody
      .map(
        (m) => `<article class="card mission" data-id="${esc(m.id)}">
          <div class="m-top">
            <div><span class="m-id">${esc(m.id)}</span><h3>${esc(m.codename)}</h3></div>
            <span class="stamp s-${esc(m.classification).toLowerCase().replace(/\s/g, '-')}">${esc(m.classification)}</span>
          </div>
          <div class="m-dest">${icon('globe', 16)} ${esc(m.destination)} <span class="status">${esc(m.status)}</span></div>
          <p>${esc(m.summary)}</p>
          <button class="btn small ghost toggle">${icon('chevron-down', 14)} Ver expediente</button>
          <div class="dossier" hidden>
            <h4>${icon('users', 16)} Tripulación</h4>
            <ul>${m.crew.map((c) => `<li>${esc(c.name)} <small>${esc(c.specialty)}</small></li>`).join('')}</ul>
            <h4>${icon('gem', 16)} Hallazgos</h4>
            <ul>${m.findings.map((f) => `<li>${esc(f)}</li>`).join('')}</ul>
            <div class="secret">${icon('eye-off', 18)}<div><b>Nota clasificada</b><p>${esc(m.classifiedNote)}</p></div></div>
          </div>
        </article>`
      )
      .join('')}</div>`;

  view.querySelectorAll('.mission .toggle').forEach((b) =>
    b.addEventListener('click', () => {
      const d = b.parentElement.querySelector('.dossier');
      d.hidden = !d.hidden;
      b.classList.toggle('open', !d.hidden);
      b.lastChild.textContent = d.hidden ? ' Ver expediente' : ' Ocultar expediente';
    })
  );
}

// ── Pantalla: Nueva misión (requiere mission.write) ─────────

function renderNew() {
  if (CONFIG.mode !== 'open' && !getSession()) return lockScreen('new');
  const head = heading('file-plus-2', 'Proponer una misión', 'Formulario para registrar una nueva misión ante el centro de control.', 'POST /api/missions', 'mission.write');
  view.innerHTML =
    head +
    `<div class="note">${icon('info', 16)}<div>Este formulario es de mentira: el servidor responde siempre lo mismo y <b>no guarda nada</b>.
      Lo importante es el permiso que pide: solo un <b>admin</b> puede enviarlo.</div></div>
    <form id="missionForm" class="form card" autocomplete="off">
      <label>Nombre clave<input name="name" placeholder="Ej. Aurora Verde" required maxlength="60"></label>
      <label>Destino<input name="destination" placeholder="Ej. Marte · Valles Marineris" required maxlength="80"></label>
      <label>Objetivo<textarea name="objective" rows="4" placeholder="¿Qué se espera encontrar? (sin spoilers alienígenas)" required maxlength="400"></textarea></label>
      <div class="form-actions">
        <button class="btn primary" type="submit">${icon('send', 16)} Enviar propuesta</button>
      </div>
    </form>
    <div id="formResult"></div>`;

  $('#missionForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.currentTarget;
    const btn = form.querySelector('button[type=submit]');
    btn.disabled = true;
    $('#formResult').innerHTML = loading('Enviando propuesta…');
    const payload = Object.fromEntries(new FormData(form));
    const r = await api('POST', '/api/missions', { body: payload });
    btn.disabled = false;
    if (r.ok) {
      $('#formResult').innerHTML = `<div class="okcard">
        <div class="ec-head">${icon('circle-check', 22)}<h3>${r.status} · Propuesta recibida</h3></div>
        <p>${esc(r.resBody?.message || 'El servidor aceptó la propuesta.')}</p>
        <p class="mono">ID asignado: <b>${esc(r.resBody?.id || '—')}</b> · Estado: ${esc(r.resBody?.status || '—')}</p>
        <div class="ec-actions"><button class="btn ghost" data-act="see-req">${icon('terminal', 16)} Ver la petición</button></div>
      </div>`;
      form.reset();
    } else {
      $('#formResult').innerHTML = errorCard(r, 'mission.write');
    }
  });
}

// ── Piezas comunes ──────────────────────────────────────────

function heading(ic, title, subtitle, endpoint, access) {
  return `<section class="page-head">
    <div class="ph-ic">${icon(ic, 26)}</div>
    <div><h1>${esc(title)}</h1><p>${esc(subtitle)}</p></div>
    <div class="ph-meta"><code>${esc(endpoint)}</code><span class="access">${icon(access === 'PÚBLICO' ? 'globe' : 'lock', 13)} ${esc(access)}</span></div>
  </section>`;
}

// ── Navegación ──────────────────────────────────────────────

const ROUTES = {
  '#/news': ['news', renderNews],
  '#/missions': ['missions', renderMissions],
  '#/missions/new': ['new', renderNew],
};

function route() {
  const [name, fn] = ROUTES[window.location.hash] || ROUTES['#/news'];
  document.querySelectorAll('#nav a').forEach((a) => a.classList.toggle('active', a.dataset.route === name));
  fn();
}

// ── Arranque ────────────────────────────────────────────────

hydrateIcons();
initSpace();
initPanel();
renderTopbar();
window.addEventListener('hashchange', () => {
  banner();
  route();
});
route();

if (CONFIG.mode === 'secure') {
  handleCallback().then((cb) => {
    if (!cb) return;
    // Mostrar la bitácora de la fase en la que terminamos
    document.querySelector('.layout').classList.add('panel-open');
    showTab('flow');
    if (cb.error) banner('error', cb.error);
    else {
      banner('ok', 'Sesión iniciada. La app ya tiene su llave.');
      const back = sessionStorage.getItem('orion.return') || '#/missions';
      if (window.location.hash !== back) {
        window.location.hash = back; // dispara 'hashchange' y se pinta la pantalla
        renderTopbar();
        return;
      }
    }
    renderTopbar();
    route();
  });
}
