# MoviesForever Web App — Implementation Plan

## Overview

Build a **React 19 + TypeScript + Vite 7 + Tailwind CSS v4** web application in `web/` that mirrors the mobile app's architecture and flow, using the landing page's indigo/violet theme, Firebase Web SDK for real-time data, and localStorage for client-side persistence.

The website clones the mobile app's screens and data model. No files in `app/`, `admin/`, `landing/`, or `worker/` are modified.

**Important:**
- **NO REFERRAL SYSTEM** — no referral mentions, screens, earnings, codes, or "Share & Earn" anywhere in the web app.
- The design is **website-style, NOT mobile-app-style** — a top navigation bar (like the landing page), no bottom nav bar.

## Architecture Summary

| Layer | Technology | Rationale |
|---|---|---|
| Framework | React 19 + TypeScript | Same stack as landing/admin |
| Build | Vite 7 | Matches landing/admin; fast HMR, optimized builds |
| Styling | Tailwind CSS v4 (CSS-first) | Matches landing; reuse `@theme` tokens |
| Routing | React Router v7 | SPA with nested routes mirroring mobile nav |
| Data | Firebase Web SDK (Firestore) | Direct real-time listeners, 1:1 with mobile app |
| API | Cloudflare Worker (redeem, signed-url) | Existing POST endpoints for auth-related calls |
| Persistence | localStorage | Unlock info, install flag — mirrors DataStore |
| Player | HTML5 `<video>` element | Native browser streaming; ExoPlayer equivalent |
| Icons | lucide-react | Already in landing/admin dependencies |

## Key Decisions

- **Directory:** `web/` (new directory at monorepo root)
- **Data access:** Firebase Web SDK directly (real-time listeners, like the mobile app)
- **Downloads tab:** Omitted (web browsers handle streaming natively; no offline download model)
- **Persistence:** localStorage (unlock info, install flag)
- **Referral system:** Completely omitted — no referral screens, earnings, codes, or any mention
- **Navigation:** Website-style top navbar (like the landing page) with Home / Search / Profile links — **no bottom nav bar**

## Folder Structure

```
web/
├── index.html
├── package.json
├── vite.config.ts
├── postcss.config.js
├── tsconfig.json
├── .env                          # VITE_FIREBASE_*, VITE_WORKER_URL, VITE_R2_PUBLIC_URL
├── public/
│   └── favicon.ico
└── src/
    ├── main.tsx                  # React root + BrowserRouter
    ├── App.tsx                   # Route definitions + layout wrapper
    ├── index.css                 # Tailwind v4 @theme (landing palette) + global styles
    ├── vite-env.d.ts
    │
    ├── types/
    │   └── index.ts              # All TypeScript interfaces (mirrors mobile data models)
    │
    ├── config/
    │   └── firebase.ts           # Firebase app init + Firestore instance
    │
    ├── hooks/
    │   ├── useFirestoreCollection.ts   # Generic real-time Firestore collection hook
    │   ├── useFirestoreDoc.ts          # Generic real-time Firestore document hook
    │   ├── useUnlock.ts                # localStorage unlock state + Firebase user lookup
    │   ├── useInstall.ts               # localStorage install flag
    │   └── useSearch.ts                # Client-side movie search/filter logic
    │
    ├── lib/
    │   ├── workerApi.ts          # fetch() wrappers for Cloudflare Worker endpoints
    │   └── utils.ts              # formatDate, formatFileSize, generateId/Code helpers
    │
    ├── context/
    │   └── AppContext.tsx         # Global app state provider (combines all hooks, like AppViewModel)
    │
    ├── components/
    │   ├── layout/
    │   │   ├── AppLayout.tsx     # Main layout: sticky top navbar + content area
    │   │   ├── Navbar.tsx        # Website-style top nav (links + profile, hamburger on mobile)
    │   │   └── Footer.tsx        # Simple site footer (logo, copyright, support link)
    │   ├── ui/
    │   │   ├── Button.tsx        # BrandButton (indigo gradient) + BrandOutlinedButton
    │   │   ├── Card.tsx          # Glass card component (bg-white/5, backdrop-blur)
    │   │   ├── Input.tsx         # Styled input field
    │   │   ├── Badge.tsx         # Pill badge (NEW, FREE, etc.)
    │   │   ├── Modal.tsx         # Dialog/modal wrapper
    │   │   ├── Spinner.tsx       # Loading spinner
    │   │   ├── Toast.tsx         # Toast notifications
    │   │   └── GlowOrb.tsx       # Decorative gradient blob (from landing)
    │   ├── movie/
    │   │   ├── MoviePoster.tsx   # Poster card with image, badge, hover overlay
    │   │   ├── MovieGrid.tsx     # Responsive grid of MoviePoster cards
    │   │   ├── SectionRow.tsx    # Horizontal scrollable movie row
    │   │   └── BannerCarousel.tsx # Auto-advancing banner carousel
    │   └── common/
    │       ├── AdminNote.tsx     # Renders admin promo note
    │       └── BackButton.tsx    # Circular back navigation button
    │
    └── pages/
        ├── SplashScreen.tsx      # Branding + routing decision
        ├── WelcomeScreen.tsx     # One-time welcome + CTA
        ├── LockScreen.tsx        # Plan cards + sign-in dialog (code redemption)
        ├── PaymentInstructions.tsx # Checkout details
        ├── HomeScreen.tsx        # Banners, curated sections, trending, offer banner
        ├── SearchScreen.tsx      # Filterable movie grid
        ├── ProfileScreen.tsx     # Account summary, notifications, settings entry
        ├── MovieDetailScreen.tsx # Hero image, metadata, watch/trailer/unlock CTAs
        ├── PlayerScreen.tsx      # HTML5 video player with controls
        ├── CategoryBrowseScreen.tsx # Movies filtered by category
        ├── CelebrationScreen.tsx # Post-unlock celebration
        ├── SettingsScreen.tsx    # WhatsApp group, reset unlock
        ├── PausedScreen.tsx      # Admin-paused account notice
        └── NotificationsScreen.tsx # In-app notification feed
```

## Implementation Phases

### Phase 1: Project Setup & Foundation

1. **Initialize project** — `package.json`, `vite.config.ts`, `postcss.config.js`, `tsconfig.json`, `index.html`, `.env.example`
2. **Tailwind theme** — `src/index.css` with `@theme` block copying landing's brand-50..700 palette, glassmorphism utilities, keyframe animations (fade-up, float, pulse-glow, pulse-dot), `.text-gradient`, `.no-scrollbar`
3. **Types** — Data model interfaces matching mobile app's Kotlin data classes (Movie, Category, Genre, Banner, TrendingItem, PricingSettings, PaymentDetails, ContactDetails, UserAccount, AppNotification, AppShareLink, UnlockInfo, etc.). **ReferralEarnings, BonusDeal, BonusStatus are intentionally excluded.**
4. **Firebase config** — `firebase.ts` initializing Firebase app + exporting `db` (Firestore), same project as admin panel
5. **Custom hooks** — `useFirestoreCollection` (real-time `onSnapshot` listener), `useFirestoreDoc`, `useUnlock` (localStorage CRUD + observable), `useInstall`
6. **Worker API** — `workerApi.ts` with `redeem(id, username)` and `getSignedUrl(movieId)` POST wrappers
7. **AppContext** — Combines all collection hooks into a single context (mirrors `AppViewModel.AppUiState`): movies, categories, genres, banners, trending, pricing, payment details, contact details, notifications, app sharing, unlock state, user account. **No referral earnings.** Uses `useMemo`/`useCallback` for memoization.

### Phase 2: Layout & Navigation

1. **App.tsx** — React Router with nested routes; splash screen as entry, conditional redirects based on unlock state
2. **AppLayout** — Responsive shell with a sticky top navbar (website-style) and `<Outlet>` for content. Max-width `max-w-7xl` container for content like the landing page.
3. **Navbar** — Sticky top bar: logo + "MoviesForever" wordmark (left), nav links (Home, Search, Profile) + unlock/sign-in status (right, desktop). On mobile: hamburger menu (like the landing page Navbar). Transparent → glass background (`bg-[#0b0b18]/85 backdrop-blur`) on scroll.
4. **Footer** — Simple footer: logo, tagline, WhatsApp support link, copyright bar.
5. **Global paused guard** — In AppContext or a layout route, watch `userAccount?.paused` and redirect to `/paused` if true (mirrors NavHost's `LaunchedEffect`).

### Phase 3: Auth & Unlock Flow

1. **SplashScreen** — Check localStorage for install/unlock flags. If not installed → Welcome. If installed but not unlocked → Lock. If unlocked → Home.
2. **WelcomeScreen** — Brand hero, "Start Browsing" CTA, mark installed in localStorage, route to Lock.
3. **LockScreen** — Lifetime Access card + Free Trial card, feature grid (2×2), sign-in dialog (Code ID + Username inputs), calls Worker `POST /redeem`, saves unlock to localStorage + sets up Firestore user listener.
4. **PaymentInstructions** — Shows pricing, bank/payment details from Firestore settings, WhatsApp support link.

### Phase 4: Core Content Screens

1. **HomeScreen** — BannerCarousel (auto-advancing, 5s interval), offer banner (shown when not unlocked, from `pricing.note`), curated section rows (Recently Added, Hot, All-Time Hit, Hit of This Year — filtered from `movie.sections`), Free to Watch row, Trending Now grid. Lazy-loads sections below fold.
2. **SearchScreen** — Search input, category chips (single-select), genre chips (multi-select), "Free only" toggle, responsive movie grid with curated-priority sorting (mirrors mobile logic).
3. **MovieDetailScreen** — Full-width hero image with gradient scrim, back button, badge, title, metadata row (IMDb rating, year, language, free badge), genre chips, action buttons (Watch Now / Watch Trailer / Unlock Lifetime Access), overview section, available dubs.
4. **CategoryBrowseScreen** — Category name header + responsive movie grid for the selected category.
5. **ProfileScreen** — Avatar header, username, status badge (Lifetime Member / Free Access), account summary (member since date), Notifications row (badge count), Settings card. When locked: Unlock CTA card instead. **No referral/earnings content.**

### Phase 5: Player & Playback

1. **PlayerScreen** — HTML5 `<video>` element with:
   - Custom overlay controls (auto-hide after 4s): back button, title, audio track selector (if multi-track), subtitle toggle, fullscreen toggle
   - Aspect ratio resize modes via CSS `object-fit` toggle (Fit/Zoom/Fill/Fixed Width/Fixed Height)
   - Error overlay with retry/back buttons
   - Calls `POST /signed-url` to get streamable URL
   - Responsive: fills viewport on mobile, centered with max-width on desktop

### Phase 6: Secondary Screens

1. **CelebrationScreen** — Animated celebration with confetti effect, "You're all set!" message, auto-dismiss after 5s or continue to Home. **No share/referral content.**
2. **SettingsScreen** — WhatsApp group link, reset unlock (clears localStorage), about info.
3. **PausedScreen** — Full-screen notice: "Your account has been paused", contact support message.
4. **NotificationsScreen** — List of admin notifications targeting the user's group (free/paid/paused), sorted by date.

### Phase 7: Performance Optimization

- **React.lazy + Suspense** — Lazy-load all page components; initial bundle only includes App + Lock/Splash
- **Virtualized grid** — For movie grids with 100+ items, use intersection observer or a lightweight virtual list
- **Image optimization** — `loading="lazy"` on all poster/thumbnail images, explicit width/height to prevent layout shift
- **Firestore listener optimization** — Detach listeners when user is on a different page (e.g., disconnect trending listener when deep in player). Use `useFirestoreCollection` with an `enabled` flag.
- **Memoization** — `useMemo` for expensive filter/sort operations (curated section sorting, search filtering). `useCallback` for event handlers passed to child components.
- **Code splitting** — Each page is a lazy chunk; player page is a separate chunk since it loads a large video player
- **CSS optimization** — Tailwind v4's CSS-first config means unused styles are purged at build time. Glassmorphism utilities defined once in `@theme`.
- **Preconnect hints** — In `index.html`: preconnect to Firestore (`firestore.googleapis.com`), R2 CDN domain, and Worker domain

### Phase 8: Responsive Design

- **Mobile-first** — All layouts start at mobile width (single-column grids, hamburger menu in navbar)
- **Tablet breakpoint** — 2-column movie grids
- **Desktop breakpoint** — 3-4 column movie grids, full nav links visible in the top navbar, centered content with `max-w-7xl`
- **Player** — Full-bleed on mobile, letterboxed on desktop
- **Navigation** — Top navbar throughout (no bottom nav bar on any viewport)

## Design Tokens (from landing page)

```css
--color-brand-50: #eef2ff;    /* Lightest tint */
--color-brand-100: #e0e7ff;
--color-brand-300: #a5b4fc;
--color-brand-400: #818cf8;   /* Hover states */
--color-brand-500: #6366f1;   /* Primary CTA */
--color-brand-600: #4f46e5;   /* Active/pressed */
--color-brand-700: #4338ca;

Background: #0b0b18 (near-black navy)
Text: #e5e7eb (gray-200)
Headings: #ffffff (white)
Glass cards: bg-white/5 backdrop-blur border-white/10
Gradient text: #818cf8 → #a78bfa → #f0abfc
```

## Data Flow (mirrors mobile app)

```
Firestore (real-time) ──→ useFirestoreCollection hooks ──→ AppContext ──→ Pages
                                                                       ↓
localStorage ←──────────── useUnlock / useInstall ←────────── LockScreen
                                                                       ↓
Cloudflare Worker ────────→ workerApi.redeem() / getSignedUrl() ←── PlayerScreen
```

## Mobile App Routes → Web Routes Mapping

| Mobile Route | Web Route | Notes |
|---|---|---|
| `splash` | `/` (SplashScreen) | Entry, routing decision |
| `welcome` | `/welcome` | One-time first launch |
| `lock` | `/lock` | Code redemption |
| `payment_instructions` | `/lock/payment` | Checkout details |
| `home` | `/` (HomeScreen) | Banner carousel, shelves, trending |
| `search` | `/search` | Filterable grid |
| `downloads` | — | Omitted (web-native streaming) |
| `profile` | `/profile` | Account summary, notifications, settings |
| `movie/{movieId}` | `/movie/:movieId` | Detail + watch/unlock CTAs |
| `category/{categoryId}` | `/category/:categoryId` | Browse by category |
| `player/{movieId}?trailer=` | `/player/:movieId?trailer=` | HTML5 video player |
| ~~`referral`~~ | — | Omitted (no referral system) |
| `celebration` | `/celebration` | Post-unlock celebration |
| `settings` | `/settings` | WhatsApp group, reset unlock |
| `paused` | `/paused` | Global pause guard |
| `notifications` | `/notifications` | Notification feed |

## Files NOT Modified

- `app/` — Mobile app untouched
- `admin/` — Admin panel untouched
- `landing/` — Landing page untouched
- `worker/` — Backend untouched (existing endpoints are sufficient)

## Estimated File Count

~40 new files in `web/`

## Build & Run Commands

```json
{
  "scripts": {
    "dev": "vite --port 3002",
    "build": "tsc && vite build",
    "preview": "vite preview"
  }
}
```

Dev server on port **3002** (3000 = admin, 3001 = landing).