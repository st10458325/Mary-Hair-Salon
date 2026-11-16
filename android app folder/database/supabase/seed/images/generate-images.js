#!/usr/bin/env node
/**
 * Generates the original category + service illustrations as SVG files.
 *
 *   node generate-images.js [outDir]      # default outDir = ./svg
 *
 * Every picture is drawn from code (no stock photos, no real people), so there are no
 * licence or privacy issues, and the set stays consistent with the app's dark/orange theme.
 * Each category has its own accent colour; its services inherit it.
 * render-images.js turns the SVGs into JPGs, upload-images.js puts them in Supabase Storage.
 */
const fs = require('fs');
const path = require('path');

const OUT = process.argv[2] || path.join(__dirname, 'svg');
fs.mkdirSync(OUT, { recursive: true });

const SKIN1 = '#c0784a', SKIN2 = '#8f5733', HAIR = '#171010', HAIR_HI = '#5a3f32', CREAM = '#f7f1ea', ORANGE = '#e8551c';
const STEEL = '#dfe5ea', STEEL2 = '#8d99a4';

const ACCENT = {
  braids: '#ff8a4c', cornrows: '#f0b030', locs: '#e0602c', weaves: '#e0527f', natural: '#c9a227',
  relaxer: '#2fb3a8', colour: '#a766f0', cuts: '#4f9be8', kids: '#ff78b0', treatments: '#3fd08f',
};

// deterministic pseudo-random so re-running gives identical files
function rng(seed) { let a = seed >>> 0; return () => { a |= 0; a = (a + 0x6D2B79F5) | 0; let t = Math.imul(a ^ (a >>> 15), 1 | a); t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t; return ((t ^ (t >>> 14)) >>> 0) / 4294967296; }; }

// ---------- building blocks ----------
const sparkle = (x, y, r, c) => `<path d="M${x} ${y - r}Q${x} ${y} ${x + r} ${y}Q${x} ${y} ${x} ${y + r}Q${x} ${y} ${x - r} ${y}Q${x} ${y} ${x} ${y - r}Z" fill="${c}" opacity=".85"/>`;
const star = (x, y, r, c) => { const p = []; for (let i = 0; i < 10; i++) { const a = -Math.PI / 2 + i * Math.PI / 5, rr = i % 2 ? r * 0.45 : r; p.push(`${(x + rr * Math.cos(a)).toFixed(1)},${(y + rr * Math.sin(a)).toFixed(1)}`); } return `<polygon points="${p.join(' ')}" fill="${c}"/>`; };
const drop = (x, y, s, c, o = 0.95) => `<path d="M${x} ${y - 18 * s}C${x + 13 * s} ${y - 2 * s} ${x + 13 * s} ${y + 13 * s} ${x} ${y + 13 * s}C${x - 13 * s} ${y + 13 * s} ${x - 13 * s} ${y - 2 * s} ${x} ${y - 18 * s}Z" fill="${c}" opacity="${o}"/>`;

const backdrop = acc => `<defs>
<radialGradient id="bg" cx="50%" cy="42%" r="80%"><stop offset="0" stop-color="${acc}" stop-opacity=".40"/><stop offset=".55" stop-color="#1b1210"/><stop offset="1" stop-color="#0b0908"/></radialGradient>
<linearGradient id="skin" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="${SKIN1}"/><stop offset="1" stop-color="${SKIN2}"/></linearGradient>
<linearGradient id="hairg" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#2d1f19"/><stop offset="1" stop-color="${HAIR}"/></linearGradient>
<linearGradient id="cloth" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#3d2e28"/><stop offset="1" stop-color="#1a1210"/></linearGradient>
<linearGradient id="steel" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#f4f7f9"/><stop offset="1" stop-color="${STEEL2}"/></linearGradient>
</defs>
<rect width="800" height="600" fill="url(#bg)"/>
<circle cx="400" cy="300" r="255" fill="${acc}" opacity=".10"/>
<circle cx="400" cy="300" r="255" fill="none" stroke="${acc}" stroke-opacity=".35" stroke-width="3"/>
${sparkle(110, 120, 18, acc)}${sparkle(695, 478, 22, acc)}${sparkle(665, 105, 12, acc)}${sparkle(125, 470, 10, acc)}`;

const body = acc => `<path d="M170 600C170 495 280 445 400 445C520 445 630 495 630 600Z" fill="url(#cloth)"/>
<path d="M355 360V440Q400 482 445 440V360Z" fill="${SKIN2}"/>
<path d="M336 452Q400 506 464 452" fill="none" stroke="${acc}" stroke-width="6" opacity=".9" stroke-linecap="round"/>`;

const head = (face = true) => `<ellipse cx="308" cy="292" rx="14" ry="27" fill="${SKIN2}"/><ellipse cx="492" cy="292" rx="14" ry="27" fill="${SKIN2}"/>
<ellipse cx="400" cy="285" rx="92" ry="112" fill="url(#skin)"/>` + (face ? `
<path d="M354 292q16 12 32 0M414 292q16 12 32 0" stroke="#3a2016" stroke-width="4.5" fill="none" stroke-linecap="round"/>
<path d="M378 344q22 17 44 0" stroke="#5a2a18" stroke-width="4.5" fill="none" stroke-linecap="round"/>
<path d="M400 296q-6 22 0 30" stroke="#6b3a22" stroke-width="3" fill="none" stroke-linecap="round" opacity=".55"/>` : '');

const capFront = (col = 'url(#hairg)') => `<path d="M306 274C294 150 358 116 400 116C442 116 506 150 494 274C482 216 452 188 400 188C348 188 318 216 306 274Z" fill="${col}"/>`;

function strand(x0, y0, cx, cy, x1, y1, w, o = {}) {
  const col = o.col || HAIR, hi = o.hi || HAIR_HI;
  const d = `M${x0.toFixed(1)} ${y0.toFixed(1)}Q${cx.toFixed(1)} ${cy.toFixed(1)} ${x1.toFixed(1)} ${y1.toFixed(1)}`;
  let s = `<path d="${d}" stroke="${col}" stroke-width="${w}" fill="none" stroke-linecap="round"/>`;
  s += o.dash === false
    ? `<path d="${d}" stroke="${hi}" stroke-width="${(w * 0.28).toFixed(1)}" fill="none" opacity=".65" transform="translate(${-w * 0.18} 0)"/>`
    : `<path d="${d}" stroke="${hi}" stroke-width="${(w * 0.6).toFixed(1)}" fill="none" stroke-dasharray="${(w * 0.42).toFixed(1)} ${(w * 0.58).toFixed(1)}"/>`;
  if (o.bead) s += `<circle cx="${x1.toFixed(1)}" cy="${y1.toFixed(1)}" r="${(w * 0.62).toFixed(1)}" fill="${o.bead}"/>`;
  return s;
}
const scalpPt = t => [400 + t * 88, 285 - 112 * Math.sqrt(Math.max(0, 1 - t * t)) * 0.97 + 6];

// ---------- tools / props ----------
function scissors(x, y, s = 1, rot = 0, acc = ORANGE) {
  return `<g transform="translate(${x} ${y}) rotate(${rot}) scale(${s})">
<polygon points="0,0 170,-16 172,-7 10,8" fill="url(#steel)"/><polygon points="0,0 170,16 172,7 10,-8" fill="url(#steel)" opacity=".92"/>
<path d="M0 0L-30 -24M0 0L-30 24" stroke="${STEEL2}" stroke-width="9" stroke-linecap="round"/>
<circle cx="-52" cy="-34" r="27" fill="none" stroke="${acc}" stroke-width="10"/><circle cx="-52" cy="34" r="27" fill="none" stroke="${acc}" stroke-width="10"/>
<circle cx="0" cy="0" r="8" fill="${STEEL2}"/></g>`;
}
function comb(x, y, s = 1, rot = 0, acc = ORANGE) {
  let t = ''; for (let i = 0; i < 16; i++) t += `<rect x="${6 + i * 11}" y="30" width="5" height="44" rx="2.5" fill="${acc}"/>`;
  return `<g transform="translate(${x} ${y}) rotate(${rot}) scale(${s})"><rect x="0" y="0" width="190" height="40" rx="10" fill="${acc}"/>${t}<rect x="8" y="9" width="174" height="6" rx="3" fill="#fff" opacity=".25"/></g>`;
}
function jar(x, y, s = 1, acc = ORANGE, open = false) {
  return `<g transform="translate(${x} ${y}) scale(${s})">
<rect x="-70" y="-10" width="140" height="100" rx="20" fill="${CREAM}"/><rect x="-70" y="30" width="140" height="26" fill="${acc}"/>
<path d="M-48 10Q-30 -2 -12 10" stroke="${acc}" stroke-width="5" fill="none" stroke-linecap="round" opacity=".8"/>
${open ? `<ellipse cx="0" cy="-8" rx="62" ry="14" fill="#efe2d3"/><path d="M-40 -14Q0 -52 40 -14Q0 -4 -40 -14Z" fill="${CREAM}"/>` : `<rect x="-76" y="-34" width="152" height="30" rx="10" fill="#2a201c"/><rect x="-76" y="-34" width="152" height="8" rx="4" fill="#fff" opacity=".12"/>`}</g>`;
}
function bottle(x, y, s = 1, acc = ORANGE) {
  return `<g transform="translate(${x} ${y}) scale(${s})"><rect x="-44" y="0" width="88" height="150" rx="22" fill="${CREAM}"/><rect x="-44" y="60" width="88" height="40" fill="${acc}"/>
<rect x="-18" y="-34" width="36" height="38" rx="8" fill="#2a201c"/><rect x="-30" y="-46" width="60" height="16" rx="6" fill="#2a201c"/></g>`;
}
function flatIron(x, y, s = 1, rot = 0, acc = ORANGE) {
  return `<g transform="translate(${x} ${y}) rotate(${rot}) scale(${s})"><rect x="0" y="-22" width="230" height="18" rx="9" fill="#2b2522"/><rect x="0" y="4" width="230" height="18" rx="9" fill="#3a312d"/>
<rect x="150" y="-18" width="80" height="10" rx="5" fill="${acc}"/><rect x="150" y="8" width="80" height="10" rx="5" fill="${acc}"/><rect x="-50" y="-14" width="56" height="28" rx="12" fill="#2b2522"/></g>`;
}
function clipper(x, y, s = 1, rot = 0, acc = ORANGE) {
  let t = ''; for (let i = 0; i < 12; i++) t += `<rect x="${-46 + i * 8}" y="-82" width="5" height="22" fill="url(#steel)"/>`;
  return `<g transform="translate(${x} ${y}) rotate(${rot}) scale(${s})"><rect x="-52" y="-60" width="104" height="30" rx="6" fill="url(#steel)"/>${t}
<rect x="-52" y="-34" width="104" height="170" rx="30" fill="#2b2522"/><rect x="-14" y="-6" width="28" height="48" rx="14" fill="${acc}"/><rect x="-52" y="-34" width="104" height="10" fill="#fff" opacity=".1"/></g>`;
}
function brush(x, y, s = 1, rot = 0, col = ORANGE) {
  return `<g transform="translate(${x} ${y}) rotate(${rot}) scale(${s})"><rect x="-8" y="-10" width="16" height="130" rx="8" fill="#2b2522"/><rect x="-26" y="-64" width="52" height="60" rx="10" fill="${col}"/><rect x="-26" y="-14" width="52" height="12" fill="${STEEL2}"/></g>`;
}
function bowl(x, y, s, col) {
  return `<g transform="translate(${x} ${y}) scale(${s})"><path d="M-90 0Q-80 90 0 90Q80 90 90 0Z" fill="#2b2522"/><ellipse cx="0" cy="0" rx="90" ry="20" fill="${col}"/><ellipse cx="0" cy="-3" rx="70" ry="12" fill="#fff" opacity=".18"/></g>`;
}
function foil(x, y, rot, w = 140, h = 26) {
  return `<g transform="translate(${x} ${y}) rotate(${rot})"><rect x="0" y="0" width="${w}" height="${h}" rx="3" fill="url(#steel)"/><rect x="0" y="0" width="${w}" height="5" fill="#fff" opacity=".5"/></g>`;
}
function steam(x, y, acc, n = 3) {
  let s = ''; for (let i = 0; i < n; i++) s += `<path d="M${x + i * 34} ${y}q-14 -22 0 -44t0 -44" stroke="${acc}" stroke-width="7" fill="none" stroke-linecap="round" opacity="${0.85 - i * 0.12}"/>`;
  return s;
}

// ---------- hair shapes ----------
/** Long flowing hair (straight when amp=0). Returns {back, front}. */
function flow({ len = 540, amp = 0, col = 'url(#hairg)', hi = HAIR_HI, shine = false }) {
  const pts = (side, edge) => {
    const o = [];
    for (let y = 190; y <= len; y += 10) {
      const base = edge === 'outer' ? 262 - Math.min(0, (y - 330) * 0.02) - (y > 380 ? 10 : 0) : 308 + Math.max(0, (y - 280)) * 0.18;
      const wob = amp * Math.sin(y / 24 + (side > 0 ? 1.6 : 0));
      const x = side < 0 ? base + wob : 800 - base + wob;
      o.push([x, y]);
    }
    return o;
  };
  const panel = side => {
    const outer = pts(side, 'outer'), inner = pts(side, 'inner').reverse();
    const all = outer.concat([[side < 0 ? outer[outer.length - 1][0] + 70 : outer[outer.length - 1][0] - 70, len + 6]], inner);
    return `<path d="M${all.map(p => p[0].toFixed(1) + ' ' + p[1].toFixed(1)).join('L')}Z" fill="${col}" stroke="${col}" stroke-width="6" stroke-linejoin="round"/>`;
  };
  const lines = side => { let s = ''; for (let k = 0; k < 3; k++) { const o = pts(side, 'outer').map(p => [p[0] + (side < 0 ? 16 + k * 14 : -16 - k * 14), p[1]]); s += `<path d="M${o.map(p => p[0].toFixed(1) + ' ' + p[1].toFixed(1)).join('L')}" stroke="${hi}" stroke-width="3" fill="none" opacity="${0.55 - k * 0.12}" stroke-linecap="round"/>`; } return s; };
  const back = `<path d="M280 200C280 120 340 100 400 100C460 100 520 120 520 200L${540 - amp * 0.4} ${len - 20}L${260 + amp * 0.4} ${len - 20}Z" fill="${col}"/>`;
  const shn = shine ? `<path d="M286 250Q272 380 286 500M514 250Q528 380 514 500" stroke="#fff" stroke-width="10" fill="none" opacity=".18" stroke-linecap="round"/>` : '';
  return { back, front: panel(-1) + panel(1) + lines(-1) + lines(1) + shn };
}
function afro(col = 'url(#hairg)', big = 172, seed = 7, curlR = 10, hi = HAIR_HI) {
  const r = rng(seed); let c = '';
  for (let i = 0; i < 150; i++) {
    const a = r() * Math.PI * 2, d = Math.sqrt(r()) * (big - 14);
    c += `<circle cx="${(400 + d * Math.cos(a)).toFixed(1)}" cy="${(228 + d * Math.sin(a)).toFixed(1)}" r="${(curlR * (0.6 + r() * 0.7)).toFixed(1)}" fill="none" stroke="${hi}" stroke-width="3" opacity=".7"/>`;
  }
  let edge = ''; for (let i = 0; i < 38; i++) { const a = (i / 38) * Math.PI * 2; edge += `<circle cx="${(400 + (big - 4) * Math.cos(a)).toFixed(1)}" cy="${(228 + (big - 4) * Math.sin(a)).toFixed(1)}" r="19" fill="${col}"/>`; }
  const back = `<circle cx="400" cy="228" r="${big}" fill="${col}"/>${edge}${c}`;
  let front = ''; for (let i = 0; i < 17; i++) { const a = Math.PI * (1.08 + (i / 16) * 0.84); front += `<circle cx="${(400 + 96 * Math.cos(a)).toFixed(1)}" cy="${(272 + 118 * Math.sin(a)).toFixed(1)}" r="17" fill="${col}" stroke="${hi}" stroke-width="2" stroke-opacity=".6"/>`; }
  return { back, front };
}

// ---------- scene assemblers ----------
function figure(acc, back = '', front = '', opt = {}) {
  const inner = `${back}${body(acc)}${head(opt.face !== false)}${front}${opt.over || ''}`;
  const g = opt.scale ? `<g transform="translate(400 600) scale(${opt.scale}) translate(-400 -600)">${inner}</g>` : inner;
  return backdrop(acc) + g + (opt.extra || '');
}
function parts(acc) { let s = ''; for (let k = -3; k <= 3; k++) s += `<path d="M${400 + k * 27} 198Q${400 + k * 20} 152 ${400 + k * 9} 124" stroke="#a37556" stroke-width="2.4" fill="none" opacity=".5"/>`; return s; }

function braids({ n = 19, len = 500, w = 13, bead = null, dash = true, acc, hi = HAIR_HI }) {
  let back = '', front = '';
  for (let i = 0; i < n; i++) {
    const t = -1 + 2 * i / (n - 1), [sx, sy] = scalpPt(t);
    back += strand(sx, sy, 400 + t * 150, (sy + len) / 2, 400 + t * 175, len, w, { bead, dash, hi });
  }
  const side = [-1, -0.9, -0.8, -0.7, -0.62, 0.62, 0.7, 0.8, 0.9, 1];
  for (const t of side) { const [sx, sy] = scalpPt(t); front += strand(sx + (t < 0 ? -6 : 6), sy + 14, 400 + t * 168, (sy + len) / 2 + 20, 400 + t * 158, len + 10, w, { bead, dash, hi }); }
  return { back, front: capFront() + parts(acc) + front };
}
function locs({ n = 11, len = 480, w = 25, acc, retwist = false }) {
  let back = '', front = '';
  const lg = (x0, y0, cx, cy, x1, y1) => {
    const d = `M${x0.toFixed(1)} ${y0.toFixed(1)}Q${cx.toFixed(1)} ${cy.toFixed(1)} ${x1.toFixed(1)} ${y1.toFixed(1)}`;
    return `<path d="${d}" stroke="${HAIR}" stroke-width="${w}" fill="none" stroke-linecap="round"/><path d="${d}" stroke="${HAIR_HI}" stroke-width="${w - 4}" fill="none" stroke-dasharray="1.6 5.4"/>`;
  };
  for (let i = 0; i < n; i++) { const t = -1 + 2 * i / (n - 1), [sx, sy] = scalpPt(t); back += lg(sx, sy, 400 + t * 145 + (i % 2 ? 8 : -8), (sy + len) / 2, 400 + t * 170 + (i % 3 - 1) * 10, len - (i % 4) * 18); }
  for (const t of [-1, -0.92, -0.84, -0.76, 0.76, 0.84, 0.92, 1]) { const [sx, sy] = scalpPt(t); front += lg(sx + (t < 0 ? -6 : 6), sy + 14, 400 + t * 168, (sy + len) / 2 + 20, 400 + t * 160, len + 4 - Math.abs(t) * 20); }
  const cap = `<path d="M304 274C288 140 360 112 400 112C440 112 512 140 496 274C484 214 452 184 400 184C348 184 316 214 304 274Z" fill="url(#hairg)"/>`;
  return { back, front: cap + front };
}
function coils(acc) {
  const r = rng(11); let c = '';
  for (let row = 0; row < 5; row++) for (let col = 0; col < 7; col++) {
    const x = 322 + col * 26 + (row % 2) * 13, y = 150 + row * 22 - Math.abs(col - 3) * 5; if (Math.hypot(x - 400, (y - 190) * 1.3) > 105) continue;
    c += `<circle cx="${x}" cy="${y}" r="13" fill="${HAIR}" stroke="${HAIR_HI}" stroke-width="2"/><path d="M${x - 6} ${y}a6 6 0 1 1 6 6" stroke="${HAIR_HI}" stroke-width="2.5" fill="none" opacity=".9"/>`;
  }
  return `<path d="M304 278C290 140 360 108 400 108C440 108 510 140 496 278C484 218 452 188 400 188C348 188 316 218 304 278Z" fill="${HAIR}"/>${c}`;
}
function topView(kind, acc) {
  const lines = []; const clip = '<clipPath id="hc"><ellipse cx="400" cy="318" rx="152" ry="196"/></clipPath>';
  const hairline = 168;
  if (kind === 'straight') {
    for (let k = -4; k <= 4; k++) lines.push(`M${400 + k * 26} ${hairline}C${400 + k * 33} 250 ${400 + k * 36} 380 ${400 + k * 22} 500`);
  } else if (kind === 'freestyle') {
    for (let k = -3; k <= 3; k++) { const s = k % 2 ? 1 : -1; lines.push(`M${400 + k * 34} ${hairline}C${400 + k * 34 + 46 * s} 230 ${400 + k * 34 - 46 * s} 300 ${400 + k * 34} 350S${400 + k * 28 + 36 * s} 430 ${400 + k * 20} 500`); }
  } else { // mixed: category
    for (let k = -4; k <= 4; k++) lines.push(`M${400 + k * 26} ${hairline}C${400 + k * 38} 250 ${400 + k * 30} 360 ${400 + k * 20} 500`);
  }
  let hair = '';
  for (const d of lines) hair += `<path d="${d}" stroke="${HAIR}" stroke-width="20" fill="none" stroke-linecap="round"/><path d="${d}" stroke="${HAIR_HI}" stroke-width="12" fill="none" stroke-dasharray="6 6"/>`;
  const swirl = kind === 'freestyle' ? `<path d="M400 470m0 0a8 8 0 1 1 8 8a16 16 0 1 1 -16 -16a26 26 0 1 1 26 26" stroke="${acc}" stroke-width="6" fill="none" stroke-linecap="round"/>` : '';
  return backdrop(acc) + `<defs>${clip}</defs>
<ellipse cx="248" cy="330" rx="20" ry="40" fill="${SKIN2}"/><ellipse cx="552" cy="330" rx="20" ry="40" fill="${SKIN2}"/>
<ellipse cx="400" cy="318" rx="152" ry="196" fill="url(#skin)"/>
<path d="M400 112l-30 52h60z" fill="${SKIN1}"/><path d="M372 164Q400 178 428 164" stroke="#6b3a22" stroke-width="4" fill="none" opacity=".6"/>
<g clip-path="url(#hc)">${hair}</g>${swirl}
<path d="M262 250Q400 130 538 250" fill="none" stroke="${acc}" stroke-width="4" opacity=".0"/>`;
}

function pigtails(acc, w = 11) {
  let back = '', front = '';
  const cols = [acc, '#fff', '#ffd23f'];
  for (const sd of [-1, 1]) for (let k = 0; k < 4; k++) {
    const sx = 400 + sd * (104 + k * 3), sy = 232 + k * 10;
    front += strand(sx, sy, 400 + sd * (150 + k * 10), 360 + k * 6, 400 + sd * (140 + k * 18), 470 + k * 8, w, { bead: cols[k % 3] });
  }
  return { back, front: capFront() + parts(acc) + front };
}

// ---------- the 30 pictures ----------
const P = {};

// categories
P['cat-braids'] = () => { const b = braids({ acc: ACCENT.braids }); return figure(ACCENT.braids, b.back, b.front); };
P['cat-cornrows'] = () => topView('mixed', ACCENT.cornrows);
P['cat-locs'] = () => { const l = locs({ acc: ACCENT.locs }); return figure(ACCENT.locs, l.back, l.front); };
P['cat-weaves-wigs'] = () => { const f = flow({ amp: 9, len: 560, shine: true }); return figure(ACCENT.weaves, f.back, capFront() + `<path d="M400 120Q372 150 340 196" stroke="#a37556" stroke-width="2.4" fill="none" opacity=".6"/>` + f.front); };
P['cat-natural'] = () => { const a = afro('url(#hairg)', 172, 7, 10); return figure(ACCENT.natural, a.back, a.front); };
P['cat-relaxer'] = () => { const f = flow({ amp: 0, len: 560, shine: true }); return figure(ACCENT.relaxer, f.back, capFront() + f.front, { extra: jar(610, 360, 0.95, ACCENT.relaxer) }); };
P['cat-colour'] = () => {
  const g = `<defs><linearGradient id="dye" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#ff7a45"/><stop offset=".5" stop-color="#e0527f"/><stop offset="1" stop-color="#8e5be8"/></linearGradient></defs>`;
  const f = flow({ amp: 4, len: 500, col: 'url(#dye)', hi: '#ffd0e0' });
  return g + figure(ACCENT.colour, f.back, capFront('url(#dye)') + `<path d="M400 120Q372 150 338 198" stroke="#ffd0e0" stroke-width="3" fill="none" opacity=".7"/>` + f.front, { extra: brush(640, 330, 0.9, 25, '#e0527f') });
};
P['cat-cuts'] = () => backdrop(ACCENT.cuts) + `<g opacity=".9">${comb(190, 430, 1.25, -14, ACCENT.cuts)}</g>${scissors(330, 300, 1.5, -22, ACCENT.cuts)}
<path d="M600 190q30 40 6 90" stroke="${HAIR}" stroke-width="14" fill="none" stroke-linecap="round"/><path d="M640 220q22 30 4 70" stroke="${HAIR}" stroke-width="12" fill="none" stroke-linecap="round"/><path d="M570 330q20 28 0 58" stroke="${HAIR}" stroke-width="11" fill="none" stroke-linecap="round"/>`;
P['cat-kids'] = () => {
  const puff = (x) => `<circle cx="${x}" cy="150" r="52" fill="url(#hairg)" stroke="${HAIR_HI}" stroke-width="2"/><rect x="${x - 26}" y="180" width="52" height="12" rx="6" fill="${ACCENT.kids}"/>`;
  return figure(ACCENT.kids, puff(318) + puff(482), capFront(), { scale: 0.86, extra: star(160, 160, 24, '#ffd23f') + star(650, 140, 18, '#fff') + star(690, 320, 14, '#ffd23f') });
};
P['cat-treatments'] = () => { const f = flow({ amp: 6, len: 560, shine: true }); return figure(ACCENT.treatments, f.back, capFront() + f.front, { extra: drop(640, 250, 2.2, ACCENT.treatments) + drop(580, 330, 1.2, '#b8ffe0') + sparkle(190, 280, 20, '#fff') }); };

// services: braids
P['knotless-braids'] = () => { const b = braids({ n: 21, len: 585, w: 9, dash: false, acc: ACCENT.braids, hi: '#8a6a50' }); return figure(ACCENT.braids, b.back, b.front); };
P['box-braids'] = () => { const b = braids({ n: 17, len: 470, w: 16, bead: ACCENT.braids, acc: ACCENT.braids }); return figure(ACCENT.braids, b.back, b.front); };
// cornrows
P['straight-backs'] = () => topView('straight', ACCENT.cornrows);
P['freestyle-cornrows'] = () => topView('freestyle', ACCENT.cornrows);
// locs
P['starter-locs'] = () => figure(ACCENT.locs, '', coils(ACCENT.locs), { extra: sparkle(600, 200, 22, '#fff') });
P['loc-retwist'] = () => {
  const l = locs({ n: 10, len: 490, w: 26, acc: ACCENT.locs });
  const arrow = `<g transform="translate(640 330)" stroke="${ACCENT.locs}" stroke-width="12" fill="none" stroke-linecap="round"><path d="M-60 0a60 60 0 1 1 28 50"/><path d="M-34 70l-4 -26l26 6" /></g>`;
  return figure(ACCENT.locs, l.back, l.front, { extra: arrow + comb(120, 360, 0.9, -70, ACCENT.locs) });
};
// weaves & wigs
P['sew-in-weave'] = () => {
  const f = flow({ amp: 10, len: 570, shine: false });
  const inset = `<defs><clipPath id="ins"><circle cx="620" cy="190" r="92"/></clipPath></defs><circle cx="620" cy="190" r="98" fill="${ACCENT.weaves}"/><circle cx="620" cy="190" r="92" fill="${SKIN2}"/>
<g clip-path="url(#ins)"><path d="M520 150H720M520 190H720M520 230H720" stroke="${HAIR}" stroke-width="20"/><path d="M520 150H720M520 190H720M520 230H720" stroke="${HAIR_HI}" stroke-width="11" stroke-dasharray="6 6"/>
<path d="M540 250q20 60 -10 100M590 250q20 60 -10 100M640 250q20 60 -10 100M690 250q20 60 -10 100" stroke="#2b1d18" stroke-width="12" fill="none" stroke-linecap="round"/></g>
<path d="M565 130q30 -20 70 6t30 66" stroke="#fff" stroke-width="4" fill="none" stroke-dasharray="3 7" stroke-linecap="round"/>
<g transform="translate(655 120) rotate(35)"><rect x="-3" y="-70" width="6" height="80" rx="3" fill="url(#steel)"/><ellipse cx="0" cy="-60" rx="2" ry="7" fill="${SKIN2}"/></g>`;
  return figure(ACCENT.weaves, f.back, capFront() + `<path d="M400 120Q372 150 340 196" stroke="#a37556" stroke-width="2.4" fill="none" opacity=".6"/>` + f.front, { extra: inset });
};
P['wig-install'] = () => {
  const f = flow({ amp: 8, len: 540 });
  // mannequin head
  const m = `<ellipse cx="400" cy="290" rx="92" ry="112" fill="#e8d9c9"/><rect x="372" y="380" width="56" height="110" fill="#d8c8b8"/><path d="M300 600Q400 505 500 600Z" fill="#cdbba9"/>
<path d="M400 150C350 150 312 200 308 270" stroke="${ACCENT.weaves}" stroke-width="4" fill="none" stroke-dasharray="3 7" stroke-linecap="round" opacity="0"/>`;
  const wigTop = `<path d="M304 280C292 152 358 118 400 118C442 118 508 152 496 280C484 220 452 192 400 192C348 192 316 220 304 280Z" fill="url(#hairg)"/>
<path d="M330 214Q400 176 470 214" stroke="${ACCENT.weaves}" stroke-width="5" fill="none" stroke-dasharray="2 8" stroke-linecap="round"/>`;
  return backdrop(ACCENT.weaves) + f.back + m + f.front.replace(/<path d="M[^"]*" fill="none" stroke="#fff"[^>]*>/g, '') + wigTop
    + `<path d="M310 250Q400 150 490 250" fill="none" stroke="#fff" stroke-width="3" opacity="0"/>` + sparkle(600, 210, 22, '#fff') + sparkle(210, 250, 14, '#fff');
};
// natural
P['wash-and-go'] = () => {
  const a = afro('url(#hairg)', 170, 21, 12, '#7a5a48');
  let rain = ''; for (let i = 0; i < 6; i++) rain += drop(560 + (i % 3) * 38, 90 + Math.floor(i / 3) * 56 + (i % 2) * 12, 1.2, '#7cc8ff');
  const shower = `<path d="M560 60Q610 20 660 60Z" fill="url(#steel)"/><rect x="606" y="60" width="8" height="14" fill="${STEEL2}"/>`;
  return figure(ACCENT.natural, a.back, a.front, { extra: shower + rain });
};
P['silk-press'] = () => { const f = flow({ amp: 0, len: 570, shine: true }); return figure(ACCENT.natural, f.back, capFront() + `<path d="M400 120Q372 150 338 198" stroke="#a37556" stroke-width="2.4" fill="none" opacity=".6"/>` + f.front, { extra: flatIron(560, 470, 0.9, -28, ACCENT.natural) }); };
// relaxer
P['relaxer-full'] = () => { const f = flow({ amp: 0, len: 560, shine: true }); return figure(ACCENT.relaxer, f.back, capFront() + f.front, { extra: bottle(630, 300, 1, ACCENT.relaxer) + drop(200, 380, 1.4, ACCENT.relaxer) }); };
P['relaxer-touchup'] = () => {
  const f = flow({ amp: 0, len: 540, shine: false });
  const root = `<path d="M306 274C294 150 358 116 400 116C442 116 506 150 494 274C482 216 452 188 400 188C348 188 318 216 306 274Z" fill="url(#hairg)"/><path d="M308 250C300 170 352 128 400 128C448 128 500 170 492 250" stroke="${ACCENT.relaxer}" stroke-width="14" fill="none" opacity=".8"/>`;
  return figure(ACCENT.relaxer, f.back, root + f.front, { extra: brush(640, 300, 1.1, 30, ACCENT.relaxer) + jar(170, 400, 0.6, ACCENT.relaxer) });
};
// colour
P['full-colour'] = () => {
  const g = `<defs><linearGradient id="dye" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#ff5e8a"/><stop offset="1" stop-color="#8e3bd8"/></linearGradient></defs>`;
  const f = flow({ amp: 5, len: 520, col: 'url(#dye)', hi: '#ffc0dd' });
  return g + figure(ACCENT.colour, f.back, capFront('url(#dye)') + `<path d="M400 120Q372 150 338 198" stroke="#ffc0dd" stroke-width="3" fill="none" opacity=".7"/>` + f.front, { extra: bowl(640, 440, 0.8, '#ff5e8a') + brush(600, 300, 0.9, 35, '#8e3bd8') });
};
P['highlights'] = () => {
  const f = flow({ amp: 6, len: 540, hi: '#f3c46a' });
  let hl = ''; for (let k = 0; k < 6; k++) hl += `<path d="M${346 + k * 20} ${130 + Math.abs(k - 2.5) * 6}Q${340 + k * 24} 190 ${326 + k * 28} 230" stroke="#f3c46a" stroke-width="7" fill="none" stroke-linecap="round" opacity=".95"/>`;
  return figure(ACCENT.colour, f.back, capFront() + hl + f.front, { extra: foil(560, 190, -25) + foil(575, 235, -25) + foil(590, 280, -25) + foil(150, 330, 20, 120, 24) });
};
// cuts
P['ladies-cut'] = () => {
  // bob
  const bob = `<path d="M296 200C290 110 360 96 400 96C440 96 510 110 504 200L518 392Q400 422 282 392Z" fill="url(#hairg)"/>`;
  const front = `<path d="M306 274C294 150 358 116 400 116C442 116 506 150 494 274C480 216 450 188 400 188C350 188 320 216 306 274Z" fill="url(#hairg)"/><path d="M400 120Q372 150 338 198" stroke="#a37556" stroke-width="2.4" fill="none" opacity=".6"/>
<path d="M296 250Q292 330 286 392M504 250Q508 330 514 392" stroke="${HAIR_HI}" stroke-width="4" fill="none" opacity=".6"/>`;
  return figure(ACCENT.cuts, bob, front, { extra: scissors(585, 395, 0.9, -38, ACCENT.cuts) + `<path d="M250 420q-14 20 -6 40M300 450q-8 18 2 34" stroke="${HAIR}" stroke-width="8" fill="none" stroke-linecap="round"/>` });
};
P['fade'] = () => {
  const defs = `<defs><linearGradient id="fadeL" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#171010"/><stop offset=".55" stop-color="#171010" stop-opacity=".55"/><stop offset="1" stop-color="#171010" stop-opacity="0"/></linearGradient></defs>`;
  const top = `<path d="M312 250C300 150 352 112 400 112C448 112 500 150 488 250C478 208 456 188 400 188C344 188 322 208 312 250Z" fill="url(#hairg)"/>`;
  const sides = `<path d="M308 262C308 200 316 190 322 188L336 214L336 300L318 330Z" fill="url(#fadeL)"/><path d="M492 262C492 200 484 190 478 188L464 214L464 300L482 330Z" fill="url(#fadeL)"/>`;
  return defs + figure(ACCENT.cuts, '', top + sides, { extra: clipper(600, 400, 0.8, 28, ACCENT.cuts) });
};
// kids
P['kids-cornrows'] = () => { const p = pigtails(ACCENT.kids, 12); return figure(ACCENT.kids, p.back, p.front, { scale: 0.88, extra: star(160, 170, 22, '#ffd23f') + star(660, 150, 16, '#fff') + star(690, 330, 12, '#ffd23f') }); };
P['kids-cut'] = () => {
  const top = `<path d="M312 250C300 160 352 128 400 128C448 128 500 160 488 250C476 214 452 198 400 198C348 198 324 214 312 250Z" fill="url(#hairg)"/>`;
  return figure(ACCENT.kids, '', top, { scale: 0.88, extra: scissors(580, 330, 0.8, -35, ACCENT.kids) + star(170, 170, 22, '#ffd23f') + star(650, 140, 16, '#fff') });
};
// treatments
P['deep-condition'] = () => backdrop(ACCENT.treatments) + jar(400, 330, 1.7, ACCENT.treatments, true) + steam(330, 190, '#d6ffec', 3)
  + drop(200, 420, 1.6, ACCENT.treatments) + drop(610, 440, 1.3, '#b8ffe0') + sparkle(620, 200, 20, '#fff');
P['scalp-treatment'] = () => {
  const f = flow({ amp: 3, len: 540 });
  const fingers = side => { let s = ''; const pts = [[62, 132], [104, 156], [128, 196]];
    for (const [dx, dy] of pts) { const x = 400 + side * dx; s += `<circle cx="${x}" cy="${dy}" r="17" fill="#e0a272" stroke="${SKIN2}" stroke-width="3"/><path d="M${x - 30} ${dy}a30 30 0 0 1 30 -30" stroke="${ACCENT.treatments}" stroke-width="5" fill="none" stroke-linecap="round" opacity=".9"/><path d="M${x + 30} ${dy}a30 30 0 0 1 -30 30" stroke="${ACCENT.treatments}" stroke-width="5" fill="none" stroke-linecap="round" opacity=".9"/>`; }
    return s; };
  const waves = `<path d="M350 100q50 -34 100 0M330 74q70 -50 140 0" stroke="${ACCENT.treatments}" stroke-width="6" fill="none" stroke-linecap="round" opacity=".9"/>`;
  return figure(ACCENT.treatments, f.back, capFront() + f.front + fingers(-1) + fingers(1) + waves, { extra: sparkle(640, 200, 22, '#fff') + sparkle(160, 220, 16, '#fff') + drop(650, 380, 1.4, ACCENT.treatments) });
};

// ---------- write ----------
for (const [name, fn] of Object.entries(P)) {
  fs.writeFileSync(path.join(OUT, `${name}.svg`), `<svg xmlns="http://www.w3.org/2000/svg" width="800" height="600" viewBox="0 0 800 600">${fn()}</svg>`);
}
console.log(`wrote ${Object.keys(P).length} svgs to ${OUT}`);
