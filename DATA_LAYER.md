# Mary Hair Salon – Android data layer

Owner: Luthando Didiza (Database/Backend). Database setup: see `../supabase/README.md`.

## How the data flows

```
Supabase (PostgreSQL + Auth + REST API)  ──fetch──▶  Room (on-device cache)  ──Flow──▶  ViewModel  ──▶  Compose screen
          ▲                                                                                  │
          └────────── writes: database API functions (create_booking, set_booking_status …) ◀┘
```

* **Supabase is the source of truth.** It is a hosted PostgreSQL database with real tables and keys.
* **Room is the offline cache.** Screens only ever observe Room, so data shows instantly and still works offline. Each screen triggers a fresh download from Supabase when it opens, and after every write.
* **Writes to bookings go through database functions.** The database checks the price, the time slot, double-booking and who is allowed to do what, so a modified app can't cheat.

## Packages

| Package | What's in it |
|---|---|
| `data/model` | Domain models the UI uses (`User`, `Category`, `Service`, `Booking`), the enums, `BookingRules` (form validation, time slots), `Pricing`, `Slug` |
| `data/local` | Room: `entity/` (tables), `dao/` (queries), `AppDatabase`, `Converters`, entity mappers |
| `data/remote` | Supabase: client setup (`SupabaseProvider`), DTOs matching the database columns, table and function names (`Db`), friendly error messages |
| `data/repository` | The only classes the UI talks to: `AuthRepository`, `CatalogRepository`, `UserRepository`, `BookingRepository` |
| `di/AppContainer` | Creates the repositories once. Get it with `(application as MaryHairSalonApp).container` |

## Repository calls used by each screen

| Screen | Repository call |
|---|---|
| Login / Sign up | `authRepository.signIn(email, pw)`, `signUp(name, surname, contact, email, pw)`. Sign-up returns `SIGNED_IN` or `CHECK_EMAIL` |
| Google SSO | Get an ID token with Credential Manager (`GetGoogleIdOption`, Web client id), then `authRepository.signInWithGoogle(idToken)` |
| Forgot password | `authRepository.sendPasswordReset(email)` |
| Splash / role routing | `authRepository.isInitializing` (show a loader while true), then `authRepository.observeCurrentUser()` emits a `User` whose `role` is CUSTOMER, EMPLOYEE or MANAGER (or `null` when signed out) |
| Settings | `authRepository.updateSettings(name, surname, contact, notificationsEnabled)`, `signOut()` |
| Services grid | `catalogRepository.observeCategoriesWithCounts()` |
| Category detail | `catalogRepository.observeServicesInCategory(id)` |
| Search | `catalogRepository.search(query)` |
| Booking form: stylists | `userRepository.observeStylists()` |
| Booking form: taken times | `bookingRepository.takenTimes(stylistId, "2026-10-14")` returns e.g. `["09:00","14:00"]` |
| Booking form: input checks | `BookingRules.validate(request)` gives friendly messages to show under the form |
| Book / Reschedule | `bookingRepository.createBooking(request)`, `reschedule(bookingId, request)` |
| My Bookings | `bookingRepository.observeMyBookings(uid)`, `observeMyStatusCounts(uid)`, `cancel(id)`, pull-to-refresh: `refreshMine()` |
| Stylist schedule | `bookingRepository.observeSchedule(uid)`, `accept(id)`, `decline(id)`, `complete(id)` |
| Manager | `observeAllBookings()`, `changeStatus(id, status)`, `reassignStylist(id, stylistId)`, `userRepository.observeAllUsers()`, `setRole(uid, role, title)`, `catalogRepository.saveService/deleteService/saveCategory/deleteCategory` |

Every write returns `Result<T>`. A failure's `message` is already user-friendly (it comes from the database, e.g. "Sorry, that time slot has just been taken"), so it can go straight into a Snackbar:

```kotlin
viewModelScope.launch {
    bookingRepository.createBooking(request)
        .onSuccess { _events.emit("Booking requested!") }
        .onFailure { _events.emit(it.message ?: "Something went wrong") }
}
```

In a ViewModel, turn a Flow into state like this (see `ui/DataCheckScreen.kt`):

```kotlin
val bookings = bookingRepository.observeMyBookings(uid)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

## Setup

1. Set up Supabase as described in `../supabase/README.md`.
2. Add `SUPABASE_URL` and `SUPABASE_ANON_KEY` to `android/local.properties`. See `local.properties.example`. Use only the anon key in the app.
3. Sync Gradle and run. The temporary data-check screen should list 10 categories.

## Tests

* `./gradlew testDebugUnitTest` runs the JVM unit tests: booking validation, pricing, status rules, database-to-app mapping, error messages, slugs and the tag converter. They need no keys or network, so CI can run them.
* `supabase/migrations` was tested against PostgreSQL 16. The tests checked: booking, double-booking, every validation message, each role's permissions, reschedule, cancel then re-book, manager reassign and role changes, and anonymous access.
