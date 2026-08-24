# Team Task Manager

A full-stack task management app built with Spring Boot, React, and SQL-backed persistence.

**Live:**
- Frontend — https://productmanagement-tasks.netlify.app/
- Backend API — https://product-management-ezxz.onrender.com

> The backend runs on a free Render instance and sleeps after ~15 minutes of inactivity.
> The first request after it sleeps can take up to a minute while it wakes up.

## Stack

- Backend: Spring Boot 3, Spring Security, JWT, Spring Data JPA
- Database: H2 file database for local dev; PostgreSQL in production
- Frontend: React 18 + Vite

## Features

- Signup and login with JWT authentication
- Role-based access control for `Admin` and `Member`
- Project creation and team assignment
- Task creation, assignment, status updates, and overdue tracking
- Dashboard with project/task metrics and status breakdown
- Admin-only team role management

## Run Backend

```bash
cd backend
mvn spring-boot:run
```

Backend runs on `http://localhost:8080` using the `dev` profile, which needs no setup —
it stores data in an H2 file at `./data/taskmanager` and seeds an admin user on first run.

Local seeded admin:

- Email: `admin@owndeck.com`
- Password: `Admin@123`

H2 console:

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/taskmanager`
- Username: `sa`
- Password: empty

Useful endpoints:

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `GET /api/dashboard`
- `GET /api/projects`
- `POST /api/projects`
- `GET /api/tasks`
- `POST /api/tasks`
- `PUT /api/tasks/{taskId}`
- `GET /api/users`
- `PUT /api/users/{userId}/role`

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`.

During local development, Vite proxies `/api` requests to `http://localhost:8080`, so the browser does not need a cross-origin request to reach the Spring Boot app. To point the frontend at a different backend, set `VITE_API_BASE_URL`.

## Configuration

### Profiles

The active profile picks the datasource. `spring.profiles.default=dev`, so **the app runs
on H2 unless you set `SPRING_PROFILES_ACTIVE`** — a Postgres URL alone is ignored without it.

| Profile | Database | Notes |
| --- | --- | --- |
| `dev` (default) | H2 file | Zero config. Seeds the admin user automatically. |
| `postgres` | PostgreSQL | Used in production. Requires the datasource variables below. |
| `mysql` | MySQL | Requires the same datasource variables. |

### Environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Set to `postgres` in production. |
| `SPRING_DATASOURCE_URL` | H2 file path | JDBC URL, e.g. `jdbc:postgresql://host/db?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Database user. |
| `SPRING_DATASOURCE_PASSWORD` | empty | Database password. |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Hibernate schema management. |
| `APP_JWT_SECRET` | dev fallback | **Override in production.** The committed default is public. |
| `APP_JWT_EXPIRATION` | `86400000` | Token lifetime in ms (24h). |
| `APP_BOOTSTRAP_ADMIN_ENABLED` | `false` (`true` in `dev`) | Seeds an admin on startup. |
| `APP_BOOTSTRAP_ADMIN_EMAIL` | `admin@owndeck.com` | Seeded admin email. |
| `APP_BOOTSTRAP_ADMIN_PASSWORD` | `Admin@123` | **Override in production.** |
| `APP_BOOTSTRAP_ADMIN_FULL_NAME` | `System Admin` | Seeded admin display name. |

The bootstrap runner is idempotent — it skips seeding if a user with that email already
exists, so leaving it enabled across restarts is safe.

## Deployment

The backend deploys to Render from `backend/Dockerfile` (multi-stage: Maven build → JRE
runtime), with **Root Directory** set to `backend` and auto-deploy on push to `main`.
The frontend deploys to Netlify with `VITE_API_BASE_URL` pointed at the Render URL.

Production runs on a **Neon** PostgreSQL database rather than Render's own, because
Render allows only one free Postgres per account and free instances are deleted after
30 days. Neon's free tier has no such expiry.

To deploy against your own database, set on the service:

```
SPRING_PROFILES_ACTIVE=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>/<database>?sslmode=require
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>
APP_JWT_SECRET=<a long random string>
```

`ddl-auto=update` creates the schema on first boot, so a new empty database needs no
migration step. To get a first admin account, set `APP_BOOTSTRAP_ADMIN_ENABLED=true`
along with a strong `APP_BOOTSTRAP_ADMIN_PASSWORD` and redeploy — otherwise the fresh
database has no users and there is no way to log in.

## Validation Notes

- Passwords require at least 8 characters with letters and numbers.
- New signups are created as `ROLE_MEMBER` by default.
- Only authenticated users can access projects, tasks, and dashboard APIs.
- Only admins can change user roles.
- Tasks can only be assigned to project members, project owners, or admins.
- Failed logins return `401` with an `Invalid credentials` body.

## Project Structure

- `backend/`: Spring Boot REST API and SQL data model
- `frontend/`: React app for authentication, dashboard, projects, and tasks
