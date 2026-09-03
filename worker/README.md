# MoviesForever Cloudflare Worker

Backend for the Android app:
- `POST /redeem` — validates and burns a redemption code (atomically) and records the user.
- `POST /signed-url` — future-protection endpoint (currently returns the public video URL).
- `GET /health` — health check.

It talks to Firestore over the REST API using a Firebase service-account JWT signed
with WebCrypto (no Node SDK needed), so it runs natively on Cloudflare Workers.

## Prerequisites

- Node.js 18+ (already installed: v20).
- A Cloudflare account.
- A Firebase project with the service account for the project whose Firestore holds
  the `movies` / `codes` / `users` collections. The app uses project `moviesforever-da21d`.

## 1. Install deps

```
npm install
```

## 2. Create a Firebase service-account key

1. Firebase Console -> Project settings -> Service accounts.
2. "Generate new private key" -> downloads a `<project>-firebase-adminsdk-xxxxx.json`.
   Example: `moviesforever-da21d-firebase-adminsdk-xxxxx.json`.
3. Keep this file's **contents** (it will be stored as a Worker secret).
   It contains `client_email` and `private_key` which the Worker needs.

## 3. Authenticate wrangler with Cloudflare

```
npx wrangler login
```

(Or set the `CLOUDFLARE_API_TOKEN` / `CLOUDFLARE_ACCOUNT_ID` env vars if you prefer tokens.)

## 4. Set the Worker secrets

```
npx wrangler secret put FIREBASE_PROJECT_ID
# then paste: moviesforever-da21d

npx wrangler secret put FIREBASE_SERVICE_ACCOUNT
# then paste the ENTIRE service-account JSON (single line or multi-line both work)
```

## 5. Optionally test locally

```
npx wrangler dev
```

Then:
```
curl http://localhost:8787/health            # -> "ok"
curl -X POST http://localhost:8787/redeem -H "Content-Type: application/json" -d '{"id":"<CODE>","username":"<USERNAME>"}'
```

For local dev the two secrets must also be set (wrangler will prompt, or add a
`.dev.vars` file with `FIREBASE_PROJECT_ID=...` and `FIREBASE_SERVICE_ACCOUNT=...`).

## 6. Deploy

```
npx wrangler deploy
```

After a successful deploy you'll get a URL like `https://moviesforever.<subdomain>.workers.dev`.

## 7. Point the Android app at the Worker

Open `app/app/build.gradle.kts` and update the base URL:

```kotlin
defaultConfig {
    buildConfigField("String", "MOVIESFOREVER_WORKER_URL", "\"https://moviesforever.workers.dev/\"")
}
```

Replace the placeholder with the actual deployed URL (must end with `/`), then rebuild
the APK. Redemption ("Unlock Forever") will now work end-to-end.

## CORS

Not currently needed for the native Android app (it uses Retrofit, not a browser).
If a web client ever calls these endpoints, add CORS headers to the Worker.
