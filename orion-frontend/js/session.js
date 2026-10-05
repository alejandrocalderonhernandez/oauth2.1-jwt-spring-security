// ─────────────────────────────────────────────────────────────
//  "¿Quién soy ahora?" Resume la sesión actual según el modo.
//  Devuelve null si es un visitante (sin login).
// ─────────────────────────────────────────────────────────────
import { CONFIG } from './config.js';
import { getTokens, decodeJwt } from './oauth.js';
import { getMockRole } from './mock.js';

const toList = (v) => (Array.isArray(v) ? v : typeof v === 'string' ? v.split(/[\s,]+/) : []).filter(Boolean);
const cleanRole = (r) => String(r).replace(/^ROLE_/i, '').toUpperCase();

export function getSession() {
  if (CONFIG.mode === 'mock') {
    const role = getMockRole();
    if (role === 'visitor') return null;
    return {
      name: role,
      roles: [role.toUpperCase()],
      scopes: role === 'admin' ? ['mission.read', 'mission.write'] : ['mission.read'],
      expiresAt: null,
      jwt: null,
    };
  }

  const tokens = getTokens();
  if (!tokens) return null;
  const jwt = decodeJwt(tokens.access_token);
  const payload = jwt?.payload || {};
  const scopes = toList(tokens.scope || payload.scope || payload.scp);
  return {
    name: payload.preferred_username || payload.sub || 'usuario',
    roles: toList(payload[CONFIG.roleClaim]).map(cleanRole),
    scopes,
    expiresAt: tokens.expires_at,
    jwt, // null si el token es opaco
  };
}

export const can = (session, scope) => !!session && session.scopes.includes(scope);
