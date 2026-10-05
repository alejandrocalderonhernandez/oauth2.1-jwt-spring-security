// ─────────────────────────────────────────────────────────────
//  Ventana de "paso a paso": detiene el flujo para poder explicar
//  qué está a punto de enviar la app. Devuelve true si se continúa.
// ─────────────────────────────────────────────────────────────
import { icon } from './icons.js';

const esc = (s) =>
  String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

export function confirmStep({ phase, title, intro, rows = [], button = 'Continuar' }) {
  return new Promise((resolve) => {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
      <div class="modal" role="dialog" aria-modal="true" aria-label="${esc(title)}">
        <div class="modal-head">
          <span class="modal-phase">FASE ${phase}</span>
          <h3>${esc(title)}</h3>
        </div>
        <p class="modal-intro">${esc(intro)}</p>
        <dl class="kv">
          ${rows
            .map(([k, v]) => `<dt>${esc(k)}</dt><dd>${esc(v)}</dd>`)
            .join('')}
        </dl>
        <div class="modal-actions">
          <button class="btn ghost" data-act="cancel">Cancelar</button>
          <button class="btn primary" data-act="ok">${esc(button)} ${icon('send', 16)}</button>
        </div>
      </div>`;
    const close = (value) => {
      overlay.remove();
      document.removeEventListener('keydown', onKey);
      resolve(value);
    };
    const onKey = (e) => {
      if (e.key === 'Escape') close(false);
      if (e.key === 'Enter') close(true);
    };
    overlay.addEventListener('click', (e) => {
      const act = e.target.closest('[data-act]')?.dataset.act;
      if (act === 'ok') close(true);
      if (act === 'cancel' || e.target === overlay) close(false);
    });
    document.addEventListener('keydown', onKey);
    document.body.appendChild(overlay);
    overlay.querySelector('[data-act="ok"]').focus();
  });
}
