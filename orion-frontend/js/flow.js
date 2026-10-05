// ─────────────────────────────────────────────────────────────
//  Bitácora del flujo OAuth.
//  Guarda en qué fase va la app y qué datos reales se usaron, para
//  mostrarlo en el panel lateral. Se guarda en sessionStorage porque la
//  página se recarga al ir al login y volver (fases 2 → 4).
// ─────────────────────────────────────────────────────────────

export const PHASES = [
  { id: 0, title: 'Descubrimiento', hint: 'La app lee el manual del servidor (opcional)' },
  { id: 1, title: 'Preparar PKCE y state', hint: 'La app inventa su secreto, su huella y su número de control' },
  { id: 2, title: 'Solicitud de autorización', hint: 'La app te manda a iniciar sesión' },
  { id: 3, title: 'Login y consentimiento', hint: 'Ocurre en el servidor de login, no en la app' },
  { id: 4, title: 'Redirect de vuelta', hint: 'El servidor te devuelve a la app con el code' },
  { id: 5, title: 'Intercambio por tokens', hint: 'La app cambia el code por la llave' },
  { id: 6, title: 'Acceso al recurso', hint: 'La app usa la llave en la API' },
  { id: 7, title: 'Renovación', hint: 'La llave caduca y se renueva con la de repuesto' },
];

const KEY = 'orion.flow';
const listeners = new Set();

function load() {
  try {
    return JSON.parse(sessionStorage.getItem(KEY)) || {};
  } catch {
    return {};
  }
}

let state = load(); // { [id]: { status, details: [[clave, valor], ...] } }

function save() {
  sessionStorage.setItem(KEY, JSON.stringify(state));
  listeners.forEach((fn) => fn(state));
}

// status: 'active' | 'done' | 'error' | 'server' | 'skipped'
export function setPhase(id, status, details) {
  const prev = state[id] || {};
  state[id] = { status, details: details ?? prev.details ?? [] };
  save();
}

export function addDetails(id, details) {
  const prev = state[id] || { status: 'active', details: [] };
  state[id] = { ...prev, details: [...(prev.details || []), ...details] };
  save();
}

export function resetFlow() {
  state = {};
  save();
}

export function getFlow() {
  return state;
}

export function onFlowChange(fn) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

// Acorta valores largos para mostrarlos (sin ocultar lo importante).
export function short(value, keep = 14) {
  if (!value) return '—';
  const s = String(value);
  return s.length <= keep * 2 + 1 ? s : `${s.slice(0, keep)}…${s.slice(-6)}`;
}
