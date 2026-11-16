#!/usr/bin/env node
/**
 * Reads the JUnit XML that Gradle wrote and adds a small results table to the GitHub Actions
 * job summary (the page you see when you open a workflow run).
 * Run locally to preview:  node .github/scripts/test-summary.js
 */
const fs = require('fs');
const path = require('path');

const dir = 'app/build/test-results/testDebugUnitTest';
let total = 0, failed = 0, skipped = 0;
const rows = [];

if (fs.existsSync(dir)) {
  for (const file of fs.readdirSync(dir).filter(f => f.endsWith('.xml'))) {
    const m = fs.readFileSync(path.join(dir, file), 'utf8').match(/tests="(\d+)" skipped="(\d+)" failures="(\d+)"/);
    if (!m) continue;
    total += +m[1]; skipped += +m[2]; failed += +m[3];
    rows.push(`| ${file.replace('TEST-', '').replace('.xml', '')} | ${m[1]} | ${m[3]} |`);
  }
}

const out = [
  '### Unit test results',
  '',
  `**${total} tests, ${failed} failed, ${skipped} skipped**`,
  '',
  '| Test class | Tests | Failures |',
  '|---|---|---|',
  ...rows,
  '',
].join('\n');

if (process.env.GITHUB_STEP_SUMMARY) fs.appendFileSync(process.env.GITHUB_STEP_SUMMARY, out);
else console.log(out);
