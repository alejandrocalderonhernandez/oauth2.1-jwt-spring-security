// ─────────────────────────────────────────────────────────────
//  Servidor simulado (modo 'mock'). Imita las reglas del resource server
//  real para poder ensayar sin levantar ningún backend:
//
//    GET  /api/news      → público
//    GET  /api/missions  → requiere mission.read   (user o admin)
//    POST /api/missions  → requiere mission.write  (solo admin)
// ─────────────────────────────────────────────────────────────

export const MOCK_ROLES = ['visitor', 'user', 'admin'];

export const getMockRole = () => sessionStorage.getItem('orion.mockRole') || 'visitor';

export function setMockRole(role) {
  sessionStorage.setItem('orion.mockRole', role);
  window.dispatchEvent(new Event('orion:auth'));
}

const delay = (ms) => new Promise((r) => setTimeout(r, ms));
const getJson = (file) => fetch(`mock/${file}`).then((r) => r.json());

export async function mockServer(method, path) {
  await delay(250); // un poquito de latencia, como un servidor de verdad
  const role = getMockRole();

  if (method === 'GET' && path === '/api/news') {
    return { status: 200, body: await getJson('news.json') };
  }

  if (path === '/api/missions') {
    if (role === 'visitor') {
      return { status: 401, body: { error: 'unauthorized', message: 'Se requiere un token de acceso.' } };
    }
    if (method === 'GET') {
      return { status: 200, body: await getJson('missions.json') };
    }
    if (method === 'POST') {
      if (role !== 'admin') {
        return {
          status: 403,
          body: { error: 'insufficient_scope', message: 'Se requiere el scope mission.write.' },
        };
      }
      return { status: 201, body: await getJson('created.json') };
    }
  }

  return { status: 404, body: { error: 'not_found', message: `No existe ${method} ${path}` } };
}
