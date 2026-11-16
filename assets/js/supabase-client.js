/**
 * assets/js/supabase-client.js
 * ---------------------------------------------------------------
 * One shared Supabase client for the whole website. It uses the same
 * database as the Android app (tables in supabase/migrations).
 *
 * Fill in the two values below from Supabase dashboard →
 * Project Settings → API. The anon key is PUBLIC by design (Row Level
 * Security protects the data), so it is fine in website code.
 * NEVER put the service_role key here.
 * ---------------------------------------------------------------
 */
import { createClient } from 'https://cdn.jsdelivr.net/npm/@supabase/supabase-js@2/+esm';

const SUPABASE_URL = 'https://tpncdqhwubsqccmtpjlo.supabase.co';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRwbmNkcWh3dWJzcWNjbXRwamxvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTQ1NTg0MzUsImV4cCI6MjExMDEzNDQzNX0.NXn6MIZ-tN1gtxrdXgj45nGwVzWXWS-tIbffAGoxt2w';

export const isConfigured = !SUPABASE_URL.includes('YOUR-PROJECT-REF') && !SUPABASE_ANON_KEY.startsWith('YOUR-');
if (!isConfigured) {
  console.error('[supabase] Fill in SUPABASE_URL and SUPABASE_ANON_KEY in assets/js/supabase-client.js');
}

export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: { persistSession: true, autoRefreshToken: true, detectSessionInUrl: true },
});

/** The logged-in user, or null. Reads the saved session, so it's instant. */
export async function getUser() {
  const { data } = await supabase.auth.getSession();
  return data.session?.user ?? null;
}

/** "Ayanda Zulu" from email sign-up metadata or the Google profile; falls back to the email. */
export function displayName(user) {
  if (!user) return '';
  const m = user.user_metadata || {};
  const fromParts = [m.first_name, m.surname].filter(Boolean).join(' ');
  return (fromParts || m.full_name || m.name || user.email || 'Account').trim();
}

/** Same message style as the app. Database functions raise "USER: ..." for messages meant for people. */
export function friendlyError(err) {
  const raw = String(err?.message || err || '');
  const userMsg = raw.match(/USER:\s*([^\n]+)/);
  if (userMsg) return userMsg[1].trim();

  const authMessages = {
    invalid_credentials: 'Incorrect email or password.',
    email_not_confirmed: 'Please confirm your email address first (check your inbox).',
    user_already_exists: 'That email is already registered — try logging in instead.',
    email_exists: 'That email is already registered — try logging in instead.',
    weak_password: 'Password should be at least 8 characters.',
    over_request_rate_limit: 'Too many attempts — please wait a moment and try again.',
    over_email_send_rate_limit: 'Too many emails sent — please wait a few minutes and try again.',
    validation_failed: "That doesn't look like a valid email address.",
    email_address_invalid: "That doesn't look like a valid email address.",
  };
  if (err?.code && authMessages[err.code]) return authMessages[err.code];

  if (err?.code === '42501' || /row-level security|permission denied/i.test(raw)) {
    return "You don't have permission to do that.";
  }
  if (/Failed to fetch|NetworkError/i.test(raw)) {
    return "You're offline. Please check your connection and try again.";
  }
  return 'Something went wrong. Please try again.';
}

/** 650 -> "R650.00" */
export function formatZAR(n) {
  return 'R' + (Number(n) || 0).toFixed(2);
}

/** "2026-10-14" -> "Wed, 14 Oct 2026" (same format as the app). */
export function formatDate(iso) {
  if (!iso) return '';
  const [y, m, d] = iso.split('-').map(Number);
  const date = new Date(y, m - 1, d);
  const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  return `${days[date.getDay()]}, ${d} ${months[m - 1]} ${y}`;
}
