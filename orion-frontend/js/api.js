// ─────────────────────────────────────────────────────────────
//  Cliente de la API. Todas las llamadas pasan por aquí para:
//   • agregar el header Authorization: Bearer <token> (fase 6)
//   • renovar el token si caducó (fase 7)
//   • guardar un historial que se muestra en el panel "Peticiones"
// ─────────────────────────────────────────────────────────────
import { CONFIG } from './config.js';
import { getTokens, refresh } from './oauth.js';
import { mockServer, getMockRole } from './mock.js';
import { setPhase, short } from './flow.js';

export const requests = []; // la más reciente primero
const listeners = new Set();
export const onRequests = (fn) => (listeners.add(fn), () => listeners.delete(fn));

let seq = 0;

async function once(method, path, body, withToken) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  // ¿Mandamos token?
  let tokenLabel = null;
  if (withToken && CONFIG.mode === 'secure' && getTokens()) {
    const t = getTokens();
    headers.Authorization = `${t.token_type || 'Bearer'} ${t.access_token}`;
    tokenLabel = `${t.token_type || 'Bearer'} ${short(t.access_token, 10)}`;
  } else if (withToken && CONFIG.mode === 'mock' && getMockRole() !== 'visitor') {
    tokenLabel = `Bearer (simulado: ${getMockRole()})`;
  }

  const url = CONFIG.mode === 'mock' ? path : CONFIG.resourceServer + path;
  const started = performance.now();
  const entry = {
    id: ++seq,
    method,
    url,
    path,
    time: new Date(),
    reqHeaders: { ...headers, ...(tokenLabel ? { Authorization: tokenLabel } : {}) },
    reqBody: body,
    status: 0,
  };

  try {
    let status, data, resHeaders = {};
    if (CONFIG.mode === 'mock') {
      // En mock el "token" solo existe si el selector no es visitante y withToken es true
      const mockRoleBackup = getMockRole();
      if (!withToken) sessionStorage.setItem('orion.mockRole', 'visitor');
      try {
        ({ status, body: data } = await mockServer(method, path));
      } finally {
        sessionStorage.setItem('orion.mockRole', mockRoleBackup);
      }
    } else {
      const res = await fetch(url, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
      });
      status = res.status;
      const text = await res.text();
      try {
        data = text ? JSON.parse(text) : null;
      } catch {
        data = text;
      }
      // Solo se pueden leer los headers que el servidor exponga por CORS
      res.headers.forEach((v, k) => (resHeaders[k] = v));
    }
    Object.assign(entry, { status, resBody: data, resHeaders, ok: status >= 200 && status < 300 });
  } catch (err) {
    Object.assign(entry, {
      status: 0,
      ok: false,
      networkError: true,
      resBody: { error: 'network_error', message: String(err.message) },
    });
  }

  entry.ms = Math.round(performance.now() - started);
  requests.unshift(entry);
  if (requests.length > 30) requests.pop();
  listeners.forEach((fn) => fn(requests));

  // Fase 6 del diagrama: la app usa la llave en la API
  if (tokenLabel) {
    setPhase(6, entry.ok ? 'done' : 'error', [
      [method, entry.url],
      ['Authorization', tokenLabel],
      ['→ respuesta', `${entry.status || 'sin respuesta'} ${entry.ok ? '✓' : '✗'}`],
    ]);
  }
  return entry;
}

export async function api(method, path, { body, withToken = true } = {}) {
  // Si el access token ya caducó y hay refresh token, renovamos antes de llamar.
  const t = getTokens();
  if (withToken && CONFIG.mode === 'secure' && t?.refresh_token && t.expires_at < Date.now() + 2000) {
    try {
      await refresh();
    } catch {
      /* la sesión se cierra sola; seguimos sin token */
    }
  }

  let result = await once(method, path, body, withToken);

  // Si el servidor dice 401 pero tenemos refresh token, un intento de renovación.
  if (result.status === 401 && withToken && CONFIG.mode === 'secure' && getTokens()?.refresh_token) {
    try {
      await refresh();
      result = await once(method, path, body, withToken);
    } catch {
      /* se mostrará el 401 */
    }
  }
  return result;
}
