# GitHub Actions (CI/CD)

Four workflows live in [.github/workflows/](../.github/workflows/). They run automatically on GitHub, and every one can also be started by hand from the **Actions** tab (**Run workflow**).

| Workflow | Runs when | What it does |
|---|---|---|
| **Android CI** (`android-ci.yml`) | push to `main`, `dev` or `supabase-salon-app`; pull requests into `main` / `dev` | Runs the 37 unit tests, writes a results table to the run summary, builds the debug APK and uploads both the test reports and the APK as downloadable artifacts |
| **Android Lint** (`android-lint.yml`) | the same, but only when app or Gradle files change | Runs Android Lint (`lintDebug`) and uploads the HTML report |
| **Backend checks** (`backend-checks.yml`) | when anything under `database/` changes | **seed-data:** dry-runs the seed and fails if any table has fewer than 10 rows. **sql-files:** checks the three migrations exist, every table has Row Level Security, the double-booking index is present, and no API key / JWT is committed. **pictures:** regenerates the 30 illustrations and checks every category and service has one |
| **Release APK** (`release-apk.yml`) | a tag like `v0.1.0` is pushed, or run by hand | Runs the tests, builds the APK and publishes a GitHub Release with the APK attached and auto-generated release notes |

## One-time setup on GitHub

Repository **Settings → Secrets and variables → Actions → New repository secret** (all optional; without them the app still builds, it just shows "not configured" if you run that APK):

| Secret | Value |
|---|---|
| `SUPABASE_URL` | the project URL, `https://<ref>.supabase.co` |
| `SUPABASE_ANON_KEY` | the **anon / public** key |
| `GOOGLE_WEB_CLIENT_ID` | the Web OAuth client ID |

**Never** add the `service_role` key as a secret for these workflows. The app must not contain it.

## Making a release

```
git tag v0.1.0
git push origin v0.1.0
```

The **Release APK** workflow then appears under **Releases** with `MaryHairSalon-v0.1.0-debug.apk`. It is debug-signed. A Play Store build needs a release keystore, which is still to do.

## Run the same checks on your own machine

```
./gradlew testDebugUnitTest assembleDebug lintDebug
node database/supabase/seed/seed.js --dry-run
node database/supabase/seed/images/generate-images.js && node database/supabase/seed/images/verify-images.js
```
