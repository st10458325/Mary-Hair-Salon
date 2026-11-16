#!/usr/bin/env node
/**
 * Uploads images/jpg/*.jpg to a public Supabase Storage bucket called "salon-images".
 * Safe to re-run (files are overwritten).  Uses the service_role key from .env, like seed.js.
 *
 *   node images/generate-images.js && node images/render-images.js   # only if you changed the artwork
 *   node upload-images.js
 *   node seed.js                                                     # writes the image URLs into the tables
 */
const fs = require('fs');
const path = require('path');

const BUCKET = 'salon-images';
const dir = path.join(__dirname, 'images', 'jpg');

(function loadEnv() {
  const file = path.join(__dirname, '.env');
  if (!fs.existsSync(file)) return;
  for (const line of fs.readFileSync(file, 'utf8').split(/\r?\n/)) {
    const m = line.match(/^\s*([A-Z0-9_]+)\s*=\s*(.*)\s*$/);
    if (m && !process.env[m[1]]) process.env[m[1]] = m[2].replace(/^["']|["']$/g, '');
  }
})();

async function main() {
  const url = process.env.SUPABASE_URL, key = process.env.SUPABASE_SERVICE_ROLE_KEY;
  if (!url || !key) { console.error('[images] Set SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY in .env'); process.exit(1); }
  const { createClient } = require('@supabase/supabase-js');
  const supabase = createClient(url, key, { auth: { persistSession: false, autoRefreshToken: false } });

  const { data: buckets, error: lerr } = await supabase.storage.listBuckets();
  if (lerr) throw lerr;
  if (!buckets.some(b => b.name === BUCKET)) {
    const { error } = await supabase.storage.createBucket(BUCKET, { public: true, fileSizeLimit: '2MB', allowedMimeTypes: ['image/jpeg', 'image/png'] });
    if (error) throw error;
    console.log(`[images] created public bucket "${BUCKET}"`);
  }
  const files = fs.readdirSync(dir).filter(f => f.endsWith('.jpg'));
  for (const f of files) {
    const { error } = await supabase.storage.from(BUCKET).upload(f, fs.readFileSync(path.join(dir, f)), { contentType: 'image/jpeg', upsert: true, cacheControl: '3600' });
    if (error) throw new Error(`${f}: ${error.message}`);
  }
  console.log(`[images] uploaded ${files.length} images to ${url}/storage/v1/object/public/${BUCKET}/`);
}
main().catch(e => { console.error('[images] FAILED:', e.message || e); process.exit(1); });
