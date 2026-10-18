# Mary Hair Salon – Android app

Kotlin + Jetpack Compose. The app uses the same Supabase database as the website, and its design follows the website: dark background, frosted "glass" cards, burnt-orange accent, serif headings and the ♕ crown mark.

## Features

**Everyone**
* Email + password **sign up / log in**. Passwords are hashed by Supabase Auth and never stored by the app.
* **Continue with Google** (single sign-on) through Android's Credential Manager.
* **Forgot password** sends a reset email.
* **Role-based routing.** After login the app reads your role and opens the right home screen: customer, stylist or manager.
* **Profile & Settings:** edit your name, surname and cell number, turn booking notifications on or off, send a password-reset email, log out.
* **Help** screen (the ⓘ icon at the top) explaining how to use every feature.
* **Works offline** for viewing. Data is cached in Room; pull down on any list to refresh.

**Customer**
* **Services:** category grid with live service counts, keyword search (title, category, tags), and a "Discounted Styles & Deals" row.
* **Booking form**, laid out like the website's booking modal:
  * personal details (prefilled from your profile);
  * service and style, with real stylists from the database;
  * Incall or Outcall (with address);
  * a month calendar, and time slots where already-booked times are crossed out;
  * notes, and a price summary with the discount applied.
* **My Bookings:** filter chips with live counts (All / Pending / Upcoming / Completed / Cancelled), **Reschedule** and **Cancel**.

**Stylist (employee)**
* **My Schedule:** accept or decline new requests, complete or cancel upcoming ones.

**Manager**
* **All Bookings:** filter, change any booking's status, reassign the stylist.
* **Staff:** promote a customer to stylist (with a job title) or revert them.
* **Catalogue:** add, edit or delete categories and services (price, discount %, tags, hide from customers).

The database does all the important checks itself (see `database/supabase/migrations/003_api.sql`):
* It reads the price from the services table.
* It validates the time slot.
* It refuses double bookings.
* It only lets each role do what it's allowed to.

The app checks the same rules first so mistakes show up instantly, and shows the database's message if anything is refused.

## Setup

1. Set up Supabase as described in `database/supabase/README.md`: run the SQL files, then seed the data.
2. In Android Studio, open the `android` folder.
3. Add these lines to `android/local.properties` (see `local.properties.example`):
   ```
   SUPABASE_URL=https://<project-ref>.supabase.co
   SUPABASE_ANON_KEY=<anon public key>
   GOOGLE_WEB_CLIENT_ID=<web client id>.apps.googleusercontent.com
   ```
   Use the **Web** OAuth client ID here, the same one that's in Supabase → Authentication → Providers → Google.
4. **Google sign-in also needs** an **Android** OAuth client in Google Cloud Console, created with:
   * package name `za.co.maryhairsalon`;
   * your debug SHA-1 (Gradle panel → `app` → `Tasks` → `android` → `signingReport`, or run `./gradlew signingReport`).

   You don't paste the Android client ID anywhere; Google matches the app by package name and SHA-1.
5. **File → Sync Project with Gradle Files**, then run the app on a phone.

Demo logins (after seeding) all use the password `MaryDemo#2026`:

| Role | Email |
|---|---|
| Customer | `ayanda@example.test` |
| Stylist | `thandi@maryhairsalon.test` |
| Manager | `manager@maryhairsalon.test` |

## Project structure

```
app/src/main/java/za/co/maryhairsalon/
  MainActivity.kt          sets up the theme and the app
  di/AppContainer.kt       creates the repositories
  data/                    data layer (see DATA_LAYER.md)
    model/                 domain models + booking rules (pure Kotlin, unit-tested)
    local/                 Room cache
    remote/                Supabase client, DTOs, error messages
    repository/            the only classes screens talk to
  ui/
    AppRoot.kt             navigation + role-based routing
    RoleHome.kt            bottom-navigation tabs for each role
    theme/                 colours and typography (matches the website)
    components/            shared composables: glass cards, buttons, booking card…
    auth/                  login, sign up, forgot password, Google sign-in
    customer/              services, category, my bookings
    booking/               booking form + month calendar
    employee/              stylist schedule
    manager/               all bookings, staff, catalogue
    profile/               settings + help
```

Each screen has a ViewModel that exposes `StateFlow`s and talks only to repositories (MVVM, spec NFR-06). Messages for the user come back as friendly text and are shown in a Snackbar.

## Google sign-in (single sign-on) troubleshooting

| What you see | Cause and fix |
|---|---|
| "Google sign-in isn't set up yet" | `GOOGLE_WEB_CLIENT_ID` is missing or empty in `local.properties`. Add it, then sync Gradle and rebuild (the value is baked in at build time). |
| "Google sign-in failed. Please try again." | The Android OAuth client's package name or SHA-1 doesn't match this build. Use `za.co.maryhairsalon` and the SHA-1 of the keystore you are running with (each developer machine, and the release keystore, has its own). Also check you used the **Web** client ID, not the Android one. |
| "No Google account found on this phone" | Add a Google account in the phone's Settings → Accounts. |
| Account picker works but you are not logged in | In Supabase → Authentication → Providers → Google, tick **Skip nonce check**, and check the Web client ID and secret match Google Cloud Console. |

After a successful sign-in the new user appears in Supabase → Authentication → Users and a profile row is created automatically.

## Tests

The JVM unit tests need no keys, no network and no emulator:

```
./gradlew testDebugUnitTest
```

Use **JDK 17 or 21**. The JDK 25 bundled with the newest Android Studio is too new for Gradle 8.14 / Kotlin 2.1. On Windows, for example:

```
$env:JAVA_HOME = "C:\Users\<you>\.jdks\jbr-21.0.11"
.\gradlew.bat testDebugUnitTest
```

There are **37 tests**, all passing:

| File | Tests | Covers |
|---|---|---|
| `BookingRulesTest` | 10 | booking validation, time slots, contact numbers, status transitions, pricing |
| `MappersTest` | 10 | database-to-app mapping, role/status fallbacks, Room round trip, tag converter |
| `ValidationAndModelsTest` | 17 | form validation edge cases, database and auth error messages, slugs, model helpers |

The HTML report is written to `app/build/reports/tests/testDebugUnitTest/index.html`. A CI pipeline (Azure Pipelines or GitHub Actions) can run the same command. Instrumented tests (Room on a device) and the login/sign-up screens need an emulator or phone and are not part of this command.

## Logging

Every repository logs what it does under its own tag (`AuthRepository`, `BookingRepository`, `CatalogRepository`, `UserRepository`, `SupabaseSync`, `GoogleSignIn`). Filter Logcat by these tags to follow what the app is doing.
