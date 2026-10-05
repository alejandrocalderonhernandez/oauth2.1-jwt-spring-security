// ─────────────────────────────────────────────────────────────
//  Configuración del frontend "Orión · Control de misión"
//  Aquí es lo único que normalmente vas a tocar.
// ─────────────────────────────────────────────────────────────

// Modos de funcionamiento:
//   'mock'   → No necesita ningún servidor. Los datos salen de /mock/*.json
//              y un selector simula Visitante / User / Admin. Ideal para ensayar.
//   'open'   → Llama al resource server REAL pero SIN login ni token.
//              Úsalo el primer día, mientras muestras las rutas en Postman.
//   'secure' → Flujo completo OAuth 2.1 (Authorization Code + PKCE).
const DEFAULT_MODE = 'mock';

// También puedes cambiar de modo desde la URL: http://localhost:5173/?mode=secure
const params = new URLSearchParams(window.location.search);
if (params.get('mode')) sessionStorage.setItem('orion.mode', params.get('mode'));
const mode = sessionStorage.getItem('orion.mode') || DEFAULT_MODE;

export const CONFIG = {
  mode,

  // Servidor de autorización (Spring Authorization Server, Keycloak, etc.)
  authServer: 'http://localhost:9000',

  // Servidor de recursos (tu API de Spring con /api/news y /api/missions)
  resourceServer: 'http://localhost:8080',

  // Cliente registrado en el servidor de autorización (público, con PKCE)
  clientId: 'orion-frontend',

  // Debe coincidir EXACTAMENTE con la redirect URI registrada en el servidor.
  redirectUri: window.location.origin + '/',

  // Permisos que la app solicita al iniciar sesión.
  scopes: 'mission.read mission.write',

  // Rutas de respaldo si el servidor no publica su metadata (fase 0).
  // Estas son las rutas por defecto de Spring Authorization Server.
  fallbackEndpoints: {
    authorization_endpoint: '/oauth2/authorize',
    token_endpoint: '/oauth2/token',
  },

  // Nombre del claim donde el servidor pone el rol del usuario en el token.
  roleClaim: 'roles',
};

// "Modo paso a paso": detiene la app antes de salir al login (fase 2) y antes
// de canjear el code (fase 5), para explicar lo que está a punto de enviar.
export const stepByStep = {
  get enabled() {
    return localStorage.getItem('orion.stepByStep') !== 'off';
  },
  set enabled(v) {
    localStorage.setItem('orion.stepByStep', v ? 'on' : 'off');
  },
};
