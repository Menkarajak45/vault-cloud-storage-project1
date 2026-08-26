# Vault — Cloud File Storage (MVP)

A minimal working slice of the full spec: **auth + nested folders + file upload/download + trash/star/search**.
Backend is Java 17 + Spring Boot; frontend is React + Vite + Tailwind.

This is intentionally scoped down from the full 2-week plan — sharing/permissions, link expiry,
file versioning, and previews are **not** included yet. See "What's not built yet" below.

## Project layout

```
cloudstorage/
├── backend/     Spring Boot API (Java 17, Maven)
└── frontend/    React app (Vite, Tailwind)
```

## Backend — run it

Requirements: Java 17+, Maven (or use the included `mvnw` if you add one).

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. It uses an embedded, file-based H2 database
(`./data/clouddb`) — no Postgres/Docker setup needed to try it out. Uploaded file bytes are
stored on local disk under `./storage/`.

> ⚠️ I wasn't able to compile this in the sandbox that generated it — that environment's network
> allowlist doesn't include Maven Central, so `mvn` couldn't resolve dependencies. The code was
> written carefully against Spring Boot 3.3 / Spring Security 6 APIs, but please run
> `mvn spring-boot:run` locally as your first step and let me know if anything doesn't compile —
> happy to fix it live.

Before deploying anywhere real, replace the demo JWT secret in `application.yml`
(`app.jwt.secret`) with a securely generated one, ideally injected via an environment variable.

### API summary

| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me` |
| Folders | `GET /api/folders?folderId=`, `POST /api/folders`, `PATCH /api/folders/{id}/rename`, `PATCH /api/folders/{id}/move`, `PATCH /api/folders/{id}/star`, `POST /api/folders/{id}/trash`, `POST /api/folders/{id}/restore`, `GET /api/folders/trash`, `GET /api/folders/starred`, `GET /api/folders/search?q=` |
| Files | `POST /api/files/upload` (multipart), `GET /api/files/{id}/download`, `PATCH /api/files/{id}/rename`, `PATCH /api/files/{id}/move`, `PATCH /api/files/{id}/star`, `POST /api/files/{id}/trash`, `POST /api/files/{id}/restore`, `DELETE /api/files/{id}` (permanent), `GET /api/files/trash`, `GET /api/files/starred`, `GET /api/files/search?q=` |

All endpoints except `/api/auth/*` require `Authorization: Bearer <token>`.

## Frontend — run it

Requirements: Node 18+.

```bash
cd frontend
npm install
cp .env.example .env   # points at http://localhost:8080/api by default
npm run dev
```

Opens on `http://localhost:5173`. Sign up, then you land on **My Drive** — upload files,
create nested folders, star and trash items, and search across your drive.

`npm run build` produces a production bundle in `frontend/dist/` (verified working).

## What's built

- Email/password auth with JWT, BCrypt password hashing
- Nested folder creation, rename, move, star, trash/restore, search
- File upload (drag-and-drop, progress bar), download, rename, move, star,
  trash/restore, permanent delete, search
- Per-user storage quota tracking (5GB default, enforced on upload)
- Trash and Starred views

## Admin account & dashboard

Platform-level authorization now has two roles: `ADMIN` and `USER`. This is separate from
file-sharing permissions (`OWNER`/`EDITOR`/`VIEWER`), which aren't implemented yet.

**Creating the first admin** — no admin is hardcoded. Set two environment variables before
starting the backend for the first time:

```bash
export ADMIN_EMAIL=admin@example.com
export ADMIN_PASSWORD=change-this-password   # 8+ chars
mvn spring-boot:run
```

On startup, `AdminBootstrapRunner` checks whether an admin already exists. If not — and both
env vars are set — it creates exactly one admin with a BCrypt-hashed password (the password is
never logged). On every later startup it's a no-op, so it's safe to leave the env vars set.

If you forget to set them, the app still starts; you'll just see a log warning and no admin
account until you set the vars and restart.

**Logging in as admin:** go to `/login` and sign in with the ADMIN_EMAIL/ADMIN_PASSWORD you set.
You'll be redirected to `/admin/dashboard` instead of the regular My Drive.

**Admin dashboard** shows: total users, admins, files, folders, and storage used; a Users tab
to view everyone and enable/disable accounts (you can't disable your own); a Files tab showing
every file across all users with owner and trash status.

### Admin API

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/admin/dashboard` | Platform-wide stats |
| GET | `/api/admin/users` | List all users |
| GET | `/api/admin/users/{id}` | One user's detail |
| PUT | `/api/admin/users/{id}/status` | `{ "enabled": true/false }` — suspend/reinstate |
| GET | `/api/admin/files?trashed=` | All files across all users, optional trash filter |
| GET | `/api/admin/activities` | Returns `{ activities: [], implemented: false }` — no Activity entity exists yet, this is a stub with a stable contract for later |

All of `/api/admin/**` is enforced by Spring Security itself (`hasRole("ADMIN")`), not just a
check inside the controller — so even a bug in a controller method can't accidentally expose it.

### Testing the two roles

1. Start the backend with `ADMIN_EMAIL`/`ADMIN_PASSWORD` set, confirm the log line
   `Created initial admin account for ...`.
2. `POST /api/auth/login` as the admin → returns a JWT with `"role": "ADMIN"` in the user object.
3. `GET /api/admin/dashboard` with that token → 200 with stats.
4. Register a normal user via `/api/auth/register`, log in, and call `/api/admin/dashboard`
   with *that* token → 403 Forbidden (JSON body, not an empty response).
5. Call any protected endpoint with no token, or a garbage token → 401 Unauthorized.
6. As admin, `PUT /api/admin/users/{someUserId}/status` with `{"enabled": false}`, then try to
   log in as that user → 403 "This account has been disabled."

## Sharing & permissions

Two separate axes now exist:
- **Platform role** (`ADMIN` / `USER`) — covered above.
- **File-level permission** (`OWNER` / `EDITOR` / `VIEWER`) — new in this pass, and completely
  independent of platform role. An ADMIN has no special file access; being an admin doesn't
  grant OWNER/EDITOR/VIEWER on anyone's files.

Enforcement lives entirely in `FileAccessService` on the backend — every file endpoint
(download, rename, trash, restore, permanent delete) resolves the caller's access level from
there before doing anything, so it's enforced at the API/storage layer, not just hidden in the UI.

| Permission | Can do |
|---|---|
| OWNER | Everything: view, rename, move, star, trash/restore, permanently delete, manage shares & public links |
| EDITOR | View/download, rename, trash, restore |
| VIEWER | View/download only |

Two deliberate simplifications, called out in code comments (`FileService`, `FileAccessService`):
- **Move** is owner-only — an editor moving a file into "your" folder tree doesn't make sense
  when folders aren't shared, only individual files are.
- **Star** is owner-only — `starred` is a single boolean on the file, not per-viewer, so letting
  an editor toggle it would silently change what the owner sees too.

### Sharing with a specific person

In the UI: open a file's `⋮` menu → **Share** → enter their email → pick Editor or Viewer.
They must already have an account (sharing is by registered user, not open invite).

API: `POST /api/shares { fileId, email, permission }` (owner-only), `GET /api/shares/file/{fileId}`
to see who has access, `DELETE /api/shares/{shareId}` to revoke, `GET /api/shares/shared-with-me`
for the recipient's view.

Shared files show up under **Shared with me** in the sidebar — they do *not* appear mixed into
the recipient's own My Drive/folder tree, since folder sharing isn't implemented.

### Public links

Same Share modal → **Public link** → optional password, optional expiry in hours → generates
`https://yourapp.com/shared/{token}`. Anyone with that link can see file info; if a password is
set, they must enter it before the download button unlocks.

API (all under `/api/public-links`):
- `POST /api/public-links` (auth, owner) — create
- `GET /api/public-links/file/{fileId}` (auth, owner) — list active links
- `DELETE /api/public-links/{id}` (auth, owner) — revoke
- `GET /api/public-links/{token}/preview` (public) — filename/size/whether a password is needed
- `POST /api/public-links/{token}/unlock` (public) — check a password
- `GET /api/public-links/{token}/download?password=` (public) — streams the file

Only those three anonymous routes are `permitAll` in `SecurityConfig`, matched precisely
(`/api/public-links/*/preview`, `/*/unlock`, `/*/download`) so `POST /api/public-links` (creating
a link) still requires authentication and ownership.

Revoked or expired links return `410 Gone`; a wrong password on download returns `401`.

### What's still a TODO here

- Activity logging (endpoint exists, returns empty — no `Activity` entity yet)
- Folder-level sharing (only individual files can be shared/linked right now)
- Admin user detail page in the UI (backend endpoint exists; frontend only lists + toggles status)
- Pagination on `/api/admin/users`, `/api/admin/files` (fine at MVP scale, would matter at scale)




- Sharing (Viewer/Editor roles) and public share links with expiry/password
- File versioning, previews, activity logs, tags
- Swap local disk storage for S3/Supabase (the storage layer, `LocalFileStorage.java`,
  is isolated behind a small interface-like surface specifically so this swap is easy later)

Happy to build out any of these next — just say which.


## MVP completion updates
- Public link passwords can be changed/removed from the Share dialog without exposing the BCrypt hash.
- Search now supports type/starred filters and pagination.
- Folder moves reject descendant cycles.
- PostgreSQL can be enabled with DATABASE_URL/DB_DRIVER/DB_USERNAME/DB_PASSWORD; H2 remains the local fallback.
- Google OAuth2 login is wired through Spring Security; configure GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET.
