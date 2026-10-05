// ─────────────────────────────────────────────────────────────
//  OAuth 2.1 · Authorization Code + PKCE, escrito a mano.
//  Cada función corresponde a una fase del diagrama de la clase.
//
//  Para un proyecto real conviene usar una librería (por ejemplo
//  oidc-client-ts). Aquí está a la vista para poder explicarlo.
// ─────────────────────────────────────────────────────────────
import { CONFIG, stepByStep } from './config.js';
import { createVerifier, createChallenge, randomString } from './pkce.js';
import { setPhase, resetFlow, short } from './flow.js';
import { confirmStep } from './modal.js';

const K_TOKENS = 'orion.tokens'; // tokens recibidos
const K_TX = 'orion.tx'; // secreto y state mientras vamos al login y volvemos
const K_META = 'orion.meta'; // metadata del servidor (fase 0)

// ⚠️ Solo para la demo: los tokens se guardan en sessionStorage para que sobrevivan
// a una recarga. En producción es más seguro un backend que guarde los tokens y
// entregue al navegador solo una cookie de sesión (patrón BFF).

const notifyAuth = () => window.dispatchEvent(new Event('orion:auth'));
const readJson = (key) => {
  try {
    return JSON.parse(sessionStorage.getItem(key));
  } catch {
    return null;
  }
};

// ── Tokens ──────────────────────────────────────────────────

export const getTokens = () => readJson(K_TOKENS);

function storeTokens(data, previous) {
  const tokens = {
    access_token: data.access_token,
    // Si el servidor no manda un refresh token nuevo, conservamos el anterior.
    refresh_token: data.refresh_token || previous?.refresh_token || null,
    token_type: data.token_type || 'Bearer',
    scope: data.scope || previous?.scope || '',
    expires_at: Date.now() + (Number(data.expires_in) || 300) * 1000,
  };
  sessionStorage.setItem(K_TOKENS, JSON.stringify(tokens));
  notifyAuth();
  return tokens;
}

// Lee el contenido de un JWT (solo para mostrarlo; la app NO debe confiar en esto
// para decidir permisos reales: eso lo valida siempre el resource server).
export function decodeJwt(token) {
  try {
    const [h, p] = token.split('.');
    const dec = (s) => JSON.parse(decodeURIComponent(escape(atob(s.replace(/-/g, '+').replace(/_/g, '/')))));
    return { header: dec(h), payload: dec(p) };
  } catch {
    return null;
  }
}

// ── Fase 0 · Descubrimiento ─────────────────────────────────

async function discover() {
  const url = `${CONFIG.authServer}/.well-known/oauth-authorization-server`;
  setPhase(0, 'active', [['GET', url]]);
  try {
    const res = await fetch(url, { headers: { Accept: 'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const meta = await res.json();
    const methods = meta.code_challenge_methods_supported;
    setPhase(0, 'done', [
      ['GET', url],
      ['issuer', meta.issuer],
      ['authorization_endpoint', meta.authorization_endpoint],
      ['token_endpoint', meta.token_endpoint],
      ['code_challenge_methods_supported', methods ? methods.join(', ') : 'no informado'],
    ]);
    if (methods && !methods.includes('S256')) {
      throw new Error('El servidor no anuncia PKCE con S256.');
    }
    sessionStorage.setItem(K_META, JSON.stringify(meta));
    return meta;
  } catch (err) {
    if (String(err.message).includes('PKCE')) throw err;
    // Es opcional: si no hay metadata usamos las rutas de config.js
    const meta = {
      issuer: CONFIG.authServer,
      authorization_endpoint: CONFIG.authServer + CONFIG.fallbackEndpoints.authorization_endpoint,
      token_endpoint: CONFIG.authServer + CONFIG.fallbackEndpoints.token_endpoint,
    };
    setPhase(0, 'skipped', [
      ['No se pudo leer la metadata', String(err.message)],
      ['Se usan las rutas de config.js', meta.authorization_endpoint],
    ]);
    sessionStorage.setItem(K_META, JSON.stringify(meta));
    return meta;
  }
}

// ── Fases 1 y 2 · Preparar PKCE y mandar al usuario a iniciar sesión ─

export async function login() {
  resetFlow();
  const meta = await discover();

  // Fase 1: secreto (verifier), huella (challenge) y número de control (state)
  const verifier = createVerifier();
  const challenge = await createChallenge(verifier);
  const state = randomString(16);
  sessionStorage.setItem(K_TX, JSON.stringify({ verifier, state }));
  setPhase(1, 'done', [
    ['state (número de control)', state],
    ['code_verifier (secreto: NO sale de la app)', short(verifier, 8)],
    ['code_challenge = BASE64URL(SHA256(verifier))', challenge],
  ]);

  // Fase 2: armar la URL de /authorize
  const url = new URL(meta.authorization_endpoint);
  url.search = new URLSearchParams({
    response_type: 'code',
    client_id: CONFIG.clientId,
    redirect_uri: CONFIG.redirectUri,
    scope: CONFIG.scopes,
    state,
    code_challenge: challenge,
    code_challenge_method: 'S256',
  }).toString();
  const rows = [['Destino', `${url.origin}${url.pathname}`], ...[...url.searchParams.entries()]];
  setPhase(2, 'active', rows);

  if (stepByStep.enabled) {
    const go = await confirmStep({
      phase: 2,
      title: 'La app te manda a iniciar sesión',
      intro:
        'Esto es lo que viaja en la URL hacia el servidor de login. Fíjate: no hay contraseñas ni tokens, solo la huella (code_challenge) y el número de control (state).',
      rows,
      button: 'Ir al login',
    });
    if (!go) {
      setPhase(2, 'error', [['Cancelado', 'El usuario canceló antes de salir al login']]);
      return;
    }
  }
  setPhase(2, 'done', [...rows, ['Redirigiendo…', 'el navegador sale hacia el servidor de login']]);
  window.location.assign(url.toString());
}

// ── Fases 3, 4 y 5 · Volver del login y canjear el code ─────

export async function handleCallback() {
  const q = new URLSearchParams(window.location.search);
  if (!q.has('code') && !q.has('error')) return null;

  const cleanUrl = () => window.history.replaceState({}, '', window.location.pathname + window.location.hash);
  const tx = readJson(K_TX);
  const meta = readJson(K_META);
  sessionStorage.removeItem(K_TX); // el verifier y el state son de un solo uso

  // Fase 3 ocurrió en el servidor de login: la app solo ve el resultado.
  setPhase(3, 'server', [
    ['Qué pasó', 'El usuario escribió su contraseña y aprobó los permisos en el servidor de login.'],
    ['Lo que la app NO vio', 'la contraseña del usuario'],
  ]);

  // El servidor puede devolver un error (por ejemplo, el usuario canceló)
  if (q.get('error')) {
    setPhase(4, 'error', [
      ['error', q.get('error')],
      ['descripción', q.get('error_description') || '—'],
    ]);
    cleanUrl();
    return { error: `El servidor de login respondió: ${q.get('error')}` };
  }

  // Fase 4: verificar state (y iss si el servidor lo manda)
  const code = q.get('code');
  const stateOk = tx && q.get('state') === tx.state;
  const iss = q.get('iss');
  const issOk = !iss || !meta?.issuer || iss === meta.issuer;
  const rows4 = [
    ['code recibido', short(code, 8)],
    ['state recibido', q.get('state') || '—'],
    ['state esperado', tx?.state || '(no hay)'],
    ['¿state coincide?', stateOk ? 'sí ✓' : 'NO ✗'],
    ['iss', iss ? `${iss} ${issOk ? '✓' : '✗'}` : 'el servidor no lo envió'],
  ];
  if (!stateOk || !issOk) {
    setPhase(4, 'error', rows4);
    cleanUrl();
    return { error: 'La respuesta no coincide con la petición original. La app se detiene aquí (state o iss inválido).' };
  }
  setPhase(4, 'done', rows4);
  cleanUrl();

  // Fase 5: canjear el code por tokens, presentando el secreto original (verifier)
  const form = {
    grant_type: 'authorization_code',
    code,
    redirect_uri: CONFIG.redirectUri,
    client_id: CONFIG.clientId,
    code_verifier: tx.verifier,
  };
  const rows5 = [
    ['POST', meta.token_endpoint],
    ['grant_type', form.grant_type],
    ['code', short(code, 8)],
    ['redirect_uri', form.redirect_uri],
    ['client_id', form.client_id],
    ['code_verifier (ahora sí viaja)', short(form.code_verifier, 8)],
  ];
  setPhase(5, 'active', rows5);

  if (stepByStep.enabled) {
    const go = await confirmStep({
      phase: 5,
      title: 'La app cambia el code por una llave',
      intro:
        'Llamada directa de la app al servidor, sin pasar por la barra del navegador. Aquí viaja el secreto original: el servidor lo convierte en huella y la compara con la que recibió al inicio.',
      rows: rows5,
      button: 'Canjear code',
    });
    if (!go) {
      setPhase(5, 'error', [['Cancelado', 'No se canjeó el code']]);
      return { error: 'Canje cancelado.' };
    }
  }

  try {
    const data = await postToken(meta.token_endpoint, form);
    storeTokens(data);
    setPhase(5, 'done', [
      ...rows5,
      ['→ token_type', data.token_type],
      ['→ expires_in', `${data.expires_in} s`],
      ['→ scope concedido', data.scope || '—'],
      ['→ access_token', short(data.access_token, 10)],
      ['→ refresh_token', data.refresh_token ? short(data.refresh_token, 10) : 'no entregado'],
    ]);
    return { ok: true };
  } catch (err) {
    setPhase(5, 'error', [...rows5, ['Error', err.message]]);
    return { error: err.message };
  }
}

// ── Fase 7 · Renovar con el refresh token ───────────────────

let refreshing = null;

export function refresh() {
  if (refreshing) return refreshing; // evita renovar dos veces a la vez
  refreshing = (async () => {
    const prev = getTokens();
    const meta = readJson(K_META);
    if (!prev?.refresh_token || !meta) throw new Error('No hay refresh token disponible.');
    const rows = [
      ['POST', meta.token_endpoint],
      ['grant_type', 'refresh_token'],
      ['refresh_token', short(prev.refresh_token, 8)],
    ];
    setPhase(7, 'active', rows);
    try {
      const data = await postToken(meta.token_endpoint, {
        grant_type: 'refresh_token',
        refresh_token: prev.refresh_token,
        client_id: CONFIG.clientId,
      });
      const next = storeTokens(data, prev);
      setPhase(7, 'done', [
        ...rows,
        ['→ nuevo access_token', short(next.access_token, 10)],
        [
          '→ refresh token',
          data.refresh_token && data.refresh_token !== prev.refresh_token
            ? 'rotado: el anterior ya no sirve'
            : 'sin cambios',
        ],
      ]);
      return next;
    } catch (err) {
      setPhase(7, 'error', [...rows, ['Error', err.message]]);
      logout(false); // si no se puede renovar, hay que iniciar sesión otra vez
      throw err;
    }
  })().finally(() => (refreshing = null));
  return refreshing;
}

// ── Utilidades ──────────────────────────────────────────────

async function postToken(endpoint, params) {
  let res;
  try {
    res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded', Accept: 'application/json' },
      body: new URLSearchParams(params),
    });
  } catch {
    throw new Error(
      'No pude conectar con el servidor de autorización. ¿Está corriendo y tiene CORS habilitado para este origen?'
    );
  }
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.error_description || data.error || `El servidor respondió HTTP ${res.status}`);
  }
  return data;
}

export function logout(resetLog = true) {
  sessionStorage.removeItem(K_TOKENS);
  sessionStorage.removeItem(K_TX);
  if (resetLog) resetFlow();
  notifyAuth();
}
