// ─────────────────────────────────────────────────────────────
//  Fondo espacial: estrellas que titilan, estrellas fugaces y
//  pequeños ovnis, marcianos y satélites flotando sin rumbo.
//  Todo es decoración: va detrás del contenido y no recibe clics.
//  Si el sistema pide "reducir movimiento", queda todo quieto.
// ─────────────────────────────────────────────────────────────
import { icon } from './icons.js';

const rand = (a, b) => a + Math.random() * (b - a);
const pick = (arr) => arr[Math.floor(Math.random() * arr.length)];
const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

const STAR_COLORS = ['#ffffff', '#ffffff', '#e0d4ff', '#c4b5fd', '#ffe9a8', '#a5f3fc'];
const FLOATERS = ['ufo', 'alien', 'ufo', 'alien', 'rocket', 'satellite', 'planet', 'comet', 'moon-star', 'sparkles', 'telescope', 'atom'];
const FLOATER_COLORS = ['#b8a6ff', '#ffd166', '#67e8f9', '#c4b5fd', '#ffd166'];

function makeStars(layer, count) {
  for (let i = 0; i < count; i++) {
    const s = document.createElement('i');
    s.className = 'star';
    const size = rand(1, 2.7);
    s.style.cssText = `left:${rand(0, 100)}%;top:${rand(0, 100)}%;width:${size}px;height:${size}px;background:${pick(STAR_COLORS)};` +
      `--d:${rand(2.5, 7)}s;--dl:${-rand(0, 7)}s;--o1:${rand(0.12, 0.35)};--o2:${rand(0.7, 1)}`;
    layer.appendChild(s);
  }
}

// Cada objeto recorre puntos al azar de la pantalla, girando suavemente, y vuelve al inicio.
function makeFloater(layer, name) {
  const size = Math.round(rand(24, 46));
  const el = document.createElement('div');
  el.className = 'floater';
  el.style.color = pick(FLOATER_COLORS);
  el.style.opacity = rand(0.16, 0.34).toFixed(2);
  el.innerHTML = icon(name, size, 1.6);
  layer.appendChild(el);

  if (reduce) {
    el.style.transform = `translate(${rand(3, 94)}vw, ${rand(12, 90)}vh)`;
    return;
  }
  const point = () => `translate(${rand(-4, 98)}vw, ${rand(6, 94)}vh) rotate(${rand(-35, 35)}deg)`;
  const first = point();
  const frames = [first, point(), point(), point(), point(), first].map((transform) => ({ transform }));
  el.animate(frames, {
    duration: rand(55000, 110000),
    iterations: Infinity,
    easing: 'ease-in-out',
    delay: -rand(0, 60000), // cada uno arranca en un punto distinto de su recorrido
  });
}

// Una estrella fugaz de vez en cuando.
function shootingStars(layer) {
  if (reduce) return;
  const fire = () => {
    const el = document.createElement('div');
    el.className = 'shooting';
    layer.appendChild(el);
    const x = rand(5, 80), y = rand(2, 45), len = rand(30, 60), angle = rand(20, 40);
    const dx = len * Math.cos((angle * Math.PI) / 180), dy = len * Math.sin((angle * Math.PI) / 180);
    const anim = el.animate(
      [
        { transform: `translate(${x}vw, ${y}vh) rotate(${angle}deg) scaleX(.2)`, opacity: 0 },
        { opacity: 1, offset: 0.15 },
        { transform: `translate(${x + dx * 0.6}vw, ${y + dy * 0.6}vh) rotate(${angle}deg) scaleX(1)`, opacity: 1, offset: 0.6 },
        { transform: `translate(${x + dx}vw, ${y + dy}vh) rotate(${angle}deg) scaleX(.4)`, opacity: 0 },
      ],
      { duration: rand(900, 1500), easing: 'ease-out' }
    );
    anim.onfinish = () => el.remove();
    setTimeout(fire, rand(5000, 13000));
  };
  setTimeout(fire, rand(1500, 4000));
}

export function initSpace() {
  const layer = document.querySelector('.stars');
  if (!layer) return;
  makeStars(layer, window.innerWidth < 700 ? 70 : 150);
  const count = window.innerWidth < 700 ? 5 : 11;
  // Aseguramos que siempre haya al menos un ovni y un marciano
  const names = ['ufo', 'alien', ...Array.from({ length: count - 2 }, () => pick(FLOATERS))];
  names.forEach((n) => makeFloater(layer, n));
  shootingStars(layer);
}
