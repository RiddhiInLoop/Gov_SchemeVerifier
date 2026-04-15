# Scheme Verifier — Architecture Summary

## Overview
- Purpose: a small demo system that lets citizens register/login, check eligibility for government schemes, apply, and lets admins review applications.
- Stack: Static frontend (HTML/CSS/JS) served over HTTP, Java backend (lightweight com.sun.net.httpserver), MySQL database.

## Diagram
See the DFD diagram file: `architecture/dfd.mmd` (Mermaid). Render it with any Mermaid tool or viewer.

## Components & Files (what & why)
- Frontend
  - `js/app.js` — Client-side logic: event handlers, API calls to `/api/*`, sessionStorage management, rendering. Separates UI concerns from backend.
  - `*.html`, `css/styles.css` — Pages and styles. Serve static content to users.
- Static Server
  - `scheme-verifier/StaticServer.java` and `scheme-verifier/scripts/static.ps1` — Serve frontend over HTTP during development to avoid file:// restrictions.
- Backend (Java)
  - `scheme-verifier/backend/Main.java` — HTTP server entrypoint; registers routes/handlers for all API endpoints.
  - `RegisterHandler`, `LoginHandler`, `SchemesHandler`, `EligibilityCheckHandler`, `ApplySchemeHandler`, admin handlers — route requests to business logic.
  - `DBConnection.java` — central JDBC connection (reads env vars `DB_URL`, `DB_USER`, `DB_PASSWORD`).
  - `PasswordUtils.java` — PBKDF2 hash & verify for passwords (security best practice for stored credentials).
  - Service classes (`CitizenService`, `SchemeService`, `EligibilityService`, `ApplicationService`, `AdminService`) — encapsulate SQL and business logic.
  - Utilities: `DBReset.java`, `DBCheck.java`, `InsertTestCitizen.java` — setup, verify, and seed database for development.
- Database
  - `scheme-verifier/database/schema.sql` — schema and initial seed (tables: `Citizen`, `Scheme`, `Application`, `Admin`).

## Major Data Flows
- Registration: browser form → POST `/api/citizens/register` → `CitizenService.registerCitizen()` → INSERT into `Citizen`.
- Login: POST `/api/citizens/login` → `CitizenService.loginCitizen()` verifies hashed password → returns citizen JSON → frontend sets sessionStorage.
- View schemes: GET `/api/schemes` → `SchemeService.getAllSchemes()` → frontend lists schemes.
- Eligibility: GET `/api/eligibility/check?citizenId=X&schemeId=Y` → `EligibilityService.checkEligibility()` reads `Citizen` + `Scheme`, evaluates rules, returns result.
- Apply: POST `/api/applications/apply` → `ApplicationService.applyForScheme()` → INSERT `Application` (status=Pending).
- Admin actions: GET `/api/admin/applications` and POST `/api/admin/approve/{id}` or `/api/admin/reject/{id}` update `Application.status`.

## Why this design
- Separation of concerns: frontend handles presentation; backend enforces validation and persistence.
- Minimal dependencies: built-in Java HTTP server keeps the project simple and easy to run locally.
- Security-aware: password hashing is implemented (PBKDF2) to avoid storing plaintext passwords.

## Known Limitations & Recommended Improvements
1. Use a JSON library (Jackson or Gson) in backend — manual string assembly is error-prone.
2. Add proper session handling (JWT or server-side sessions) instead of `sessionStorage` for production.
3. Replace com.sun.net.httpserver with a framework (Spring Boot / Spark) for improved routing, DI, and testing.
4. Add input validation and sanitization on both client and server; use prepared statements (already used) and validate range/format.
5. Add HTTPS, logging, rate limiting, and monitoring for production readiness.

## How to run (dev)
1. Start MySQL and set env vars (PowerShell):
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/scheme_verifier_db"
$env:DB_USER = "root"
$env:DB_PASSWORD = "root"
```
2. Compile backend and helpers:
```powershell
# from project root
powershell -NoProfile -ExecutionPolicy Bypass -File "scheme-verifier/scripts/backend.ps1" compile
```
3. Reset DB (optional, seeds default admin):
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "scheme-verifier/scripts/backend.ps1" dbreset
```
4. Run backend:
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "scheme-verifier/scripts/backend.ps1" run
```
5. Start static server (serves frontend on port 3000):
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "scheme-verifier/scripts/static.ps1"
```
6. Open: `http://localhost:3000` in browser

## Next steps I can do for you
- Render the Mermaid DFD to an SVG/PNG and commit it.
- Produce a concise PDF architecture doc.
- Create a diagram with labeled sequence flows for a specific use-case (e.g., Apply → Admin Approve).

---
Generated: architecture/dfd.mmd and this summary (architecture/architecture_summary.md)
