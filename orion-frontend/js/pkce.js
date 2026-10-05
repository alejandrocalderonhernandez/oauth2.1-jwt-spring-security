// ─────────────────────────────────────────────────────────────
//  PKCE (RFC 7636) con la Web Crypto API del navegador.
//  Nota: crypto.subtle solo existe en "contextos seguros":
//  https:// o http://localhost (no funciona con una IP).
// ─────────────────────────────────────────────────────────────

// Convierte bytes a Base64URL (Base64 sin +, / ni =), el formato que pide PKCE.
function base64url(bytes) {
  let bin = '';
  bytes.forEach((b) => (bin += String.fromCharCode(b)));
  return btoa(bin).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

// Cadena aleatoria criptográficamente segura.
export function randomString(byteLength = 32) {
  const bytes = new Uint8Array(byteLength);
  crypto.getRandomValues(bytes);
  return base64url(bytes);
}

// code_verifier: el "secreto" de un solo uso (43 a 128 caracteres).
export function createVerifier() {
  return randomString(32); // 32 bytes → 43 caracteres
}

// code_challenge = BASE64URL(SHA256(code_verifier)): la "huella" del secreto.
export async function createChallenge(verifier) {
  const data = new TextEncoder().encode(verifier);
  const digest = await crypto.subtle.digest('SHA-256', data);
  return base64url(new Uint8Array(digest));
}
