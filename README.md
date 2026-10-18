# Mary Hair Salon

Mary Hair Salon is a salon booking platform with:
- a PHP + Firebase web experience for customers, and
- a Kotlin/Jetpack Compose Android app in `android app folder/`.

## Web app highlights

- Home, services, bookings, about, contact, login and signup pages
- Reusable PHP component-based UI (`components/`)
- Firebase Authentication for sign up / login flows
- Firestore-backed service catalogue, bookings, and contact messages
- Responsive styling in `style.css`

## Repository structure

- `index.php` – homepage
- `services.php` – service categories and searchable services
- `bookings.php` – customer booking history and status filters
- `login.php`, `signup.php`, `forgot-password.php` – auth pages
- `about.php`, `contact.php` – informational pages
- `components/` – reusable UI sections and cards
- `assets/js/firebase-config.js` – Firebase app/auth/firestore setup
- `android app folder/` – Android client (see its dedicated README)

## Run the web project locally

### Requirements
- PHP 8+
- Internet access (Firebase JS SDK is loaded from Google CDN)
- A Firebase project configured for Auth + Firestore

### Start

From the repository root:

```bash
php -S localhost:8000 -t .
```

Then open:

- `http://localhost:8000/index.php`

## Firebase setup

The web app reads Firebase configuration from:

- `assets/js/firebase-config.js`

Make sure your Firebase project has the required collections used by the app (such as categories, services, bookings, and messages) and matching security rules.

## Android app

The Android implementation and setup instructions are documented in:

- `android app folder/README.md`

## Notes

- The contact form currently stores submissions in Firestore.
- Most interactive pages fetch data client-side from Firestore.
