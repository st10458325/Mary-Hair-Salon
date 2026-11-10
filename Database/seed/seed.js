#!/usr/bin/env node
/**
 * Fills the Supabase database with simulated data: at least 10 rows in every table.
 *
 * Setup (once):
 *   1. Run the three SQL files in supabase/migrations in the Supabase SQL Editor.
 *   2. Copy .env.example to .env and paste in your project URL and service_role key
 *      (Supabase dashboard -> Project Settings -> API). NEVER commit .env.
 *   3. npm install
 *
 * Run:
 *   node seed.js --dry-run     # checks the data, writes nothing, needs no keys
 *   node seed.js               # writes to Supabase
 *
 * Safe to run more than once: every row has a fixed id, so re-running updates the
 * seed rows instead of duplicating them. Real users' data is never touched.
 */

const fs = require('fs');
const path = require('path');
const data = require('./seed-data');

const DRY_RUN = process.argv.includes('--dry-run');

function log(msg) {
  console.log(`[seed] ${msg}`);
}

/** Minimal .env reader so no extra package is needed. */
function loadEnv() {
  const file = path.join(__dirname, '.env');
  if (!fs.existsSync(file)) return;
  for (const line of fs.readFileSync(file, 'utf8').split(/\r?\n/)) {
    const m = line.match(/^\s*([A-Z0-9_]+)\s*=\s*(.*)\s*$/);
    if (m && !process.env[m[1]]) process.env[m[1]] = m[2].replace(/^["']|["']$/g, '');
  }
}

async function main() {
  const fakeIds = Object.fromEntries(data.users.map(u => [u.key, `<${u.key}>`]));
  const plan = {
    profiles: data.users.length,
    categories: data.categories.length,
    services: data.services.length,
    bookings: data.buildBookings(fakeIds).length,
    messages: data.messages.length,
  };

  if (DRY_RUN) {
    log('Dry run — nothing will be written.');
    console.table(plan);
    const short = Object.entries(plan).filter(([, n]) => n < 10);
    if (short.length) {
      console.error(`[seed] ERROR: fewer than 10 rows in: ${short.map(([k]) => k).join(', ')}`);
      process.exit(1);
    }
    return;
  }

  loadEnv();
  const url = process.env.SUPABASE_URL;
  const serviceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
  if (!url || !serviceKey) {
    throw new Error('Missing SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY. Copy .env.example to .env and fill it in.');
  }
  log(`Using Supabase project: ${url}`);

  const { createClient } = require('@supabase/supabase-js');
  // The service_role key bypasses Row Level Security. Only ever use it in scripts
  // like this one, never in the Android app or the website.
  const supabase = createClient(url, serviceKey, { auth: { persistSession: false, autoRefreshToken: false } });

  // 1. Auth accounts. The on_auth_user_created trigger creates each profile row.
  const existing = new Map();
  for (let page = 1; ; page++) {
    const { data: res, error } = await supabase.auth.admin.listUsers({ page, perPage: 1000 });
    if (error) throw error;
    res.users.forEach(u => existing.set((u.email || '').toLowerCase(), u.id));
    if (res.users.length < 1000) break;
  }

  const idByKey = {};
  for (const u of data.users) {
    const attrs = {
      email: u.email,
      password: data.DEMO_PASSWORD,
      email_confirm: true,
      user_metadata: { first_name: u.name, surname: u.surname, contact: u.contact },
    };
    const knownId = existing.get(u.email.toLowerCase());
    if (knownId) {
      const { error } = await supabase.auth.admin.updateUserById(knownId, attrs);
      if (error) throw error;
      idByKey[u.key] = knownId;
      log(`auth: updated ${u.email}`);
    } else {
      const { data: created, error } = await supabase.auth.admin.createUser(attrs);
      if (error) throw error;
      idByKey[u.key] = created.user.id;
      log(`auth: created ${u.email}`);
    }
  }

  // 2. Profiles: set name, contact, role and stylist title (upsert in case the trigger
  //    wasn't installed yet when the account was first created).
  const profiles = data.users.map(u => ({
    id: idByKey[u.key],
    email: u.email,
    name: u.name,
    surname: u.surname,
    contact: u.contact,
    role: u.role,
    title: u.title,
  }));
  await upsert(supabase, 'profiles', profiles);

  // 3. Catalogue, bookings, messages.
  await upsert(supabase, 'categories', data.categories);
  await upsert(supabase, 'services', data.services);
  await upsert(supabase, 'bookings', data.buildBookings(idByKey));
  await upsert(supabase, 'messages', data.messages);

  // 4. Report what is actually in the database now.
  const counts = {};
  for (const table of Object.keys(plan)) {
    const { count, error } = await supabase.from(table).select('*', { count: 'exact', head: true });
    if (error) throw error;
    counts[table] = count;
  }
  console.table(counts);
  log(`Done. Demo password for every seeded account: ${data.DEMO_PASSWORD}`);
  log('Manager login: manager@maryhairsalon.test');
}

async function upsert(supabase, table, rows) {
  const { error } = await supabase.from(table).upsert(rows, { onConflict: 'id' });
  if (error) throw new Error(`${table}: ${error.message}${error.details ? ` (${error.details})` : ''}`);
  log(`${table}: ${rows.length} rows`);
}

main().catch(err => {
  console.error('[seed] FAILED:', err.message || err);
  if (/relation .* does not exist|Could not find the table/i.test(String(err.message))) {
    console.error('[seed] The tables are missing. Run the SQL files in supabase/migrations first (SQL Editor).');
  }
  if (/Invalid API key|JWT/i.test(String(err.message))) {
    console.error('[seed] Check SUPABASE_SERVICE_ROLE_KEY in .env: it must be the service_role key, not the anon key.');
  }
  process.exit(1);
});
