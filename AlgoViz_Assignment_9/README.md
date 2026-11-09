# AlgoViz — DSA Visualizer | CS543 Assignment 09

AlgoViz shows data structures and algorithms visually. This submission implements **JWT authentication + role-based API protection** using **Java 21, Spring Boot, Spring Security, MySQL, JPA/Hibernate and React**. It also implements Bubble Sort and Linear Search step generation and a simple visualization UI.

**Implementation stack:** Java 21, Spring Boot, Spring Security, MySQL and React (approved by the course instructor).

## Team details
| Member | Roll number |
|---|---|
| Saurabh Singh | 20251651089 |
| Niraj kumar Gupta | 20251651066 |
| Anurag Tiwari | 20251651028 |
| Mohd Rashid | 20251651057 |

## Submission links
- GitHub repository: **Pending — https://github.com/Niraj-1804/AlgoViz/tree/main/AlgoViz_Assignment_9
- Google Drive demo video: https://drive.google.com/file/d/1f9dF_LaLYRpNQCU-l7lU5X3T-cwpkd9o/view?usp=drive_link
- Google Drive API screenshots: https://drive.google.com/drive/folders/1ph6kIMztOLi7OPoZQX2XetYLyefLnZ3Q?usp=drive_link

## Requirements
- Java **21**, Maven **3.9+**, MySQL running locally, Node.js **18+** with npm.
- A local `.env` with a secure JWT secret and two demo passwords.
- `jq` (only for the automated curl demo script).

## 1. Configure
```bash
cp .env.example .env
```
Edit `.env`, set `JWT_SECRET` to a random string **at least 32 bytes**, and set `STUDENT_PASSWORD`, `ADMIN_PASSWORD`, `DB_USER`, `DB_PASSWORD`. To generate a strong JWT secret on macOS: `openssl rand -hex 32`. Do not put quotes/spaces in values when using `scripts/start-backend.sh`. Do **not** commit `.env`. Only the root `.env.example` is tracked. `.env` contains private local passwords and the signing secret; keep it on your machine, never in Git or screenshots.

MySQL: Start MySQL. The JDBC URL defaults to `jdbc:mysql://localhost:3306/algoviz?createDatabaseIfNotExist=true`. The chosen DB user needs permission to create/use the DB. Alternatively create it once: `CREATE DATABASE algoviz;`.

## 2. Run backend
**Run the backend from the project root using only `bash scripts/start-backend.sh` for the supplied local setup.** Spring Boot does **not** automatically load the root `.env` file. The script loads and exports it; launching the app directly from an IDE or running `mvn spring-boot:run` without the same environment variables may fail because `JWT_SECRET` is missing.

```bash
bash scripts/start-backend.sh
```
The API starts at `http://localhost:8080`. On first startup, two users are created (`student` / `admin`) and **BCrypt-hashed** passwords are saved in MySQL. On later runs they are not reset if they already exist. To change a seeded password later, update it securely in the DB or use a new DB for the local demo.

## 3. Run React frontend (new Terminal tab)
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173`. The UI lets both users log in. Only ADMIN can create a visualization; both roles can call secured read endpoints. React calls the Spring Boot API.

## Routes and access rules
| Method | Path | Who may access | Result |
|---|---|---|---|
| POST | `/api/login` | Public | 200 JWT / 401 bad credentials |
| GET | `/api/algorithms` | STUDENT, ADMIN (authenticated) | 200 / 401 |
| GET | `/api/visualizations` | STUDENT, ADMIN (authenticated) | 200 / 401 |
| POST | `/api/visualizations` | ADMIN only | 201 / 401 / 403 |
| DELETE | `/api/visualizations/{id}` | ADMIN only | 204 / 401 / 403 / 404 |

### Five required demo cases
**Option A (automatic):** In another Terminal, from project root, load demo passwords without printing them:
```bash
set -a; source .env; set +a
bash scripts/demo-cases.sh
```
The command makes real HTTP requests and creates `demo/output/*.txt`. Use the actual console output for your demo screenshots, masking JWT/passwords. The script needs `jq` (`brew install jq`).

**Option B (Postman):** Login using `POST http://localhost:8080/api/login` with JSON `{ "username":"admin", "password":"your local admin password" }`. Copy the token into Postman's Bearer Token field; test `GET /api/algorithms` and `POST /api/visualizations` with `{ "algorithm":"bubble-sort", "values":[5,2,4] }`. Repeat write with STUDENT token to see 403. Remove the token to see 401.

## Answers required in the README
**Q1. Which routes need authentication / role?** `GET /api/algorithms` and `GET /api/visualizations` require authenticated STUDENT or ADMIN. `POST /api/visualizations` and `DELETE /api/visualizations/{id}` require authenticated ADMIN. `POST /api/login` is public.

**Q2. Why 401 vs 403?** 401: `GET /api/algorithms` with no Bearer JWT has no valid authentication. 403: `POST /api/visualizations` with a valid STUDENT JWT is authenticated, but STUDENT lacks ADMIN privileges. **Why not the other code?** Missing/invalid authentication is **401**, not 403; an authenticated user lacking the required role gets **403**, not 401.

**Q3. What is in JWT and why trust it?** Signed JWT claims: `sub` (user id), `role`, `username`, `iat`, `exp`. The server verifies the HMAC signature, expiration and current user's DB role before granting access; clients cannot alter the role without invalidating the signature. `JWT_SECRET` must stay out of source code / GitHub, because possession of the secret enables forged valid tokens.

## Security details
- Passwords stored only as **BCrypt hashes** (never plaintext in DB).
- JWT secret read from `JWT_SECRET` environment variable.
- Missing/invalid/expired Bearer JWT -> HTTP 401. Logged-in student attempting an admin write -> HTTP 403.
- One-hour JWT expiration; stateless Spring Security.
- The frontend holds the token in React memory only, not localStorage. This is still a **teaching demo**, not a production-auth system.

## File map
- `backend/src/main/java/com/algoviz/auth` — login API
- `backend/src/main/java/com/algoviz/security` — JWT service, auth filter, authorization
- `backend/src/main/java/com/algoviz/user` — user entity, roles, repository, seed data
- `backend/src/main/java/com/algoviz/visualization` — API, Java algorithms, saved run metadata
- `frontend/src` — React UI
- `scripts/demo-cases.sh` — automated 401/200/200/201/403 curl sequence plus STUDENT DELETE -> 403
- `demo/SCREENSHOTS_AND_VIDEO.md` — video plan and screenshot checklist

## Cross-origin requests
The backend allows frontend origins `http://localhost:5173` and `http://127.0.0.1:5173` for local development.

## Screenshots
Store verified, redacted evidence under `demo/screenshots/` (`.png` and `.jpg` are **not** git-ignored). Do not create fake test evidence; include only genuine HTTP results.

## Test
Run `cd backend && mvn test`. Commit the project except `.env`, build outputs and raw secrets. For the final submission **you must capture genuine screenshots/video**, push GitHub, paste the actual GitHub and Google Drive links above.
