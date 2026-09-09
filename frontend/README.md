# FinTrack — Frontend

The web client for [FinTrack](../README.md), a personal finance and subscription management system. Talks to the Spring Boot API in the parent `../` directory.

## Tech Stack

| Concern | Choice |
|---|---|
| Framework | React 19 + TypeScript, Vite |
| Routing | React Router |
| Server state | TanStack Query |
| Client state | Zustand (auth session, in-memory) |
| Forms | React Hook Form + Zod |
| UI | Tailwind CSS v4, shadcn/ui (Base UI primitives), lucide-react |
| Charts | Recharts |
| HTTP | Axios, with a request/response interceptor pair that handles JWT refresh |

## Architecture Notes

- **Feature-based structure** (`src/features/*`) rather than layer-based — each feature owns its pages, hooks, and schemas; shared pieces live in `src/components/common` and `src/components/layout`.
- **Centralized API layer** (`src/api/*.api.ts`) — no component calls Axios directly. Every function unwraps the backend's `ApiResponse<T>` envelope and returns the typed payload.
- **Token strategy**: the access token lives only in memory (`store/auth-store.ts`) and is lost on page reload by design; the refresh token lives in `localStorage` (the backend returns it as JSON, not an httpOnly cookie, so this is the only place it can persist) and is used to silently re-establish a session on app load. See the comments in `lib/refresh-token-storage.ts` and `api/axios.ts` for the full reasoning and the known trade-off.
- **Error messages are English and user-facing by design** — the backend's own messages are Turkish (see the backend's own code/comments), so `utils/error-message.ts` maps HTTP status codes to friendly English copy rather than surfacing raw backend text.

## Getting Started

```bash
npm install
cp .env.example .env.development   # already checked in with a working local default
npm run dev
```

Requires the backend running at the URL in `VITE_API_BASE_URL` (defaults to `http://localhost:8080/api/v1`) with CORS configured to allow this dev server's origin (`CORS_ALLOWED_ORIGINS` on the backend, defaults to `http://localhost:5173`).

## Status

Built in phases alongside the backend's own phased build, and complete: project setup, full auth flow (register/login/refresh rotation/forgot-reset password), the core app shell (responsive sidebar, dark mode), Dashboard, Transactions, Budgets, Subscriptions, Recurring Payments, Analytics (charts + smart insights), Notifications, Reports (date-range summaries + CSV export), and Settings (profile + password).
