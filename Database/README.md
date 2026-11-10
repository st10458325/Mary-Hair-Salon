# Mary Hair Salon – Supabase setup

Owner: Luthando Didiza (Database/Backend)

Supabase gives us a hosted **PostgreSQL** database (real tables, primary and foreign keys, data types), **Supabase Auth** (email + password and Google SSO) and an automatic **REST API** over the tables. The Android app talks to all three.

## Folder contents

| File | What it does |
|---|---|
| `migrations/001_schema.sql` | Creates the 5 tables, their keys and checks, the `booking_details` view, and a trigger that creates a profile for every new account |
| `migrations/002_security.sql` | Row Level Security: who can read or change which rows |
| `migrations/003_api.sql` | API functions the app calls: `create_booking`, `reschedule_booking`, `set_booking_status`, `reassign_stylist`, `set_user_role`, `taken_times` |
| `seed/seed-data.js` | The simulated data: 13 users, 10 categories, 20 services, 15 bookings, 10 messages |
| `seed/seed.js` | Uploads that data (creates the login accounts too) |

## Tables (ERD)

```
auth.users (Supabase) 1──1 profiles
profiles   1──*  bookings (customer_id)
profiles   1──*  bookings (stylist_id, role = employee)
categories 1──*  services
services   1──*  bookings
messages   (stand-alone: website contact form)
```

* **Double-booking protection.** A unique index on `(stylist_id, booking_date, booking_time)` covers active bookings (pending or upcoming), so the database itself refuses a second booking for the same slot.
* **Prices can't be faked.** `create_booking` reads the price and discount from `services`. The app never sends a price.
* **Passwords.** Supabase Auth stores them as bcrypt hashes in `auth.users`. Our tables never contain a password.

## One-time setup (about 10 minutes)

1. **Create the project.** Go to supabase.com → New project. Name it `mary-hair-salon`, choose a region close to you, and save the database password somewhere safe.
2. **Create the tables.** Open the **SQL Editor** → New query. Paste in all of `001_schema.sql` and click **Run**. Do the same for `002_security.sql`, then `003_api.sql`, in that order. Each should say "Success. No rows returned".
3. **Email settings (for testing).** Go to **Authentication → Sign In / Providers → Email** and turn off **Confirm email**. Otherwise every new sign-up has to click a link in an email before it can log in. You can turn it back on before the final demo.
4. **Seed the data:**
   ```
   cd supabase/seed
   npm install
   copy .env.example .env        (Mac/Linux: cp .env.example .env)
   ```
   Open `.env` and fill in two values from **Project Settings → API**:
   * `SUPABASE_URL`: the **Project URL**.
   * `SUPABASE_SERVICE_ROLE_KEY`: the **service_role** secret key. This key bypasses all security, so only ever use it in `.env`, never in the app or on GitHub.

   Then run:
   ```
   node seed.js --dry-run
   node seed.js
   ```
   You should see a table with 13 / 10 / 20 / 15 / 10 rows. Every demo account uses the password `MaryDemo#2026`. The manager account is `manager@maryhairsalon.test`.
5. **Connect the Android app.** In `android/local.properties`, add:
   ```
   SUPABASE_URL=https://<project-ref>.supabase.co
   SUPABASE_ANON_KEY=<the anon public key>
   ```
   Then in Android Studio: **File → Sync Project with Gradle Files** → Run.

## Google sign-in (SSO)

1. In Google Cloud Console → APIs & Services → Credentials, create two OAuth client IDs:
   * **Web application.** Copy its client ID and client secret.
   * **Android.** Package `za.co.maryhairsalon` plus your debug SHA-1. Get the SHA-1 from Android Studio → Gradle → `signingReport`.
2. In Supabase → **Authentication → Sign In / Providers → Google**: turn it on, paste the Web client ID and secret, and tick **Skip nonce check** (simplest for Android).
3. In the app, the UI gets a Google ID token with Credential Manager (`GetGoogleIdOption`, using the **Web** client ID). It then calls `authRepository.signInWithGoogle(idToken)`. The database trigger creates the user's profile automatically.

## Showing the data in the demo video

Use **Table Editor** for rows, **Authentication → Users** for accounts (passwords are hashed and never visible), and **API Docs** for the generated REST API.
