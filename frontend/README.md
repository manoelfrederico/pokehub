# Pokehub web

React 19 + TypeScript front end for the Pokehub API.

## Setup

```bash
npm install
cp .env.example .env
```

Default `VITE_API_BASE_URL=/api/v1`. With `npm run dev`, Vite proxies `/api` to `http://localhost:8080` (start the backend first).

## Scripts

| Command | Purpose |
|---------|---------|
| `npm run dev` | Dev server (port 5173) |
| `npm run build` | Production bundle in `dist/` |
| `npm run preview` | Serve production build locally |
| `npm run test` | Vitest unit tests |
| `npm run lint` | ESLint |
| `npm run typecheck` | `tsc --noEmit` |

## Auth token storage

JWT access tokens are stored in `sessionStorage` (cleared when the tab closes). This reduces persistence compared to `localStorage` but is still readable by script on the page — treat XSS as out of scope but be aware of the trade-off.

## Production (P08)

Build with `VITE_API_BASE_URL=/api/v1`. Serve `dist/` behind nginx and proxy `/api/` to the Spring Boot service so the browser uses same-origin API calls without CORS.
