#!/usr/bin/env node
/**
 * Checks that every category and service in seed-data.js has a picture (svg + jpg), and that
 * no picture is left over for something that no longer exists. Exits 1 on any problem.
 * Used by the "Backend checks" GitHub Actions workflow; run it locally with: node verify-images.js
 */
const fs = require('fs');
const path = require('path');
const { categories, services } = require('../seed-data');

const expected = new Set([...categories.map(c => `cat-${c.id}`), ...services.map(s => s.id)]);
const problems = [];

for (const [dir, ext] of [['svg', '.svg'], ['jpg', '.jpg']]) {
  const folder = path.join(__dirname, dir);
  const have = new Set(fs.existsSync(folder) ? fs.readdirSync(folder).filter(f => f.endsWith(ext)).map(f => f.slice(0, -ext.length)) : []);
  for (const name of expected) if (!have.has(name)) problems.push(`missing ${dir}/${name}${ext}`);
  for (const name of have) if (!expected.has(name)) problems.push(`unused  ${dir}/${name}${ext}`);
}

if (problems.length) { console.error(problems.join('\n')); console.error(`\n${problems.length} problem(s).`); process.exit(1); }
console.log(`OK: ${expected.size} pictures (${categories.length} categories + ${services.length} services), svg and jpg.`);
