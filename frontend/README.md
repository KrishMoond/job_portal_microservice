# HireHub — Frontend

Angular 21 SPA for the HireHub job portal. Built with standalone components, Angular Signals, OnPush change detection, and Tailwind CSS v4.

---

## Prerequisites

- Node.js 20+
- npm 9+
- Backend services running (see root README)

---

## Setup

```bash
npm install
npm start
```

App runs at `http://localhost:4200`

All `/api/**` requests are proxied to the API Gateway at `http://localhost:8080` via `proxy.conf.json`.

---

## Build

```bash
npm run build
```

Output goes to `dist/`.

---

## Project Structure

```
src/
├── app/
│   ├── core/
│   │   ├── guards/          # Auth guard
│   │   ├── interceptors/    # JWT, cache, error interceptors
│   │   └── services/        # ApiService, AuthService, ToastService
│   ├── features/
│   │   ├── auth/            # Login, Register
│   │   ├── jobs/            # Job list, Job detail, Job create, Bookmarks
│   │   ├── applications/    # My applications
│   │   ├── recruiter/       # Dashboard, Applicants, Pipeline, Company management
│   │   ├── notifications/   # Notification center
│   │   ├── companies/       # Company list and detail
│   │   ├── seeker/          # Job seeker profile
│   │   └── landing/         # Landing page
│   └── shared/
│       ├── components/      # Navbar, Toast, Skeleton, Server status banner
│       ├── directives/      # Ripple, ScrollReveal, CountUp, Tilt
│       ├── models/          # Shared TypeScript interfaces
│       └── icons.ts         # Lucide icon registry
├── styles.css               # Tailwind v4 + global design system
└── environments/            # Environment configs
```

---

## Features

### Job Seeker
- Browse and search jobs by keyword, location, and category
- Filter by job type, tech stack, and salary range
- Bookmark jobs
- Apply for jobs with resume
- Track application status
- Accept or reject job offers
- View scheduled interviews
- Upload and manage resumes
- In-app notifications

### Recruiter
- Post and manage job listings
- Create and manage company profiles
- View and filter applicants per job
- Move candidates through hiring pipeline
- Schedule interviews with meeting links
- View analytics dashboard

### General
- JWT authentication with role-based routing
- Persistent login with localStorage
- Toast notifications for all actions
- Skeleton loaders during data fetch
- Responsive design (mobile + desktop)
- OnPush change detection throughout

---

## Tech Decisions

| Decision | Reason |
|----------|--------|
| Standalone components | No NgModules, simpler imports |
| Angular Signals | Avoids NG0100 errors, fine-grained reactivity |
| OnPush everywhere | Prevents unnecessary change detection cycles |
| Tailwind CSS v4 | Utility-first, custom design tokens via `@theme {}` |
| `takeUntil(destroy$)` | Prevents memory leaks from unsubscribed observables |
| Cache interceptor | Caches only static data (companies, search) — not jobs/applications |

---

## Styling

The design system is defined in `src/styles.css` using Tailwind v4:

- `@theme {}` — custom colors, fonts, shadows, animations
- `@layer base` — body, typography defaults
- `@layer components` — `.btn`, `.card`, `.form-input`, `.badge`, `.alert`, `.modal`, `.skeleton`
- `@layer utilities` — `.glass`, `.text-gradient`, `.animate-*`, `.bento-grid`, `.glow-card`

---

## Environment

`src/environments/environment.ts`:
```ts
export const environment = {
  production: false,
  apiUrl: '/api'
};
```

The proxy (`proxy.conf.json`) forwards `/api` to `http://localhost:8080`.
