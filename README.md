# Team Task Manager

A full-stack task management app built with Spring Boot, React, and SQL-backed persistence.

## Stack

- Backend: Spring Boot 3, Spring Security, JWT, Spring Data JPA
- Database: H2 SQL database persisted to disk
- Frontend: React 18 + Vite

## Features

- Signup and login with JWT authentication
- Role-based access control for `Admin` and `Member`
- Project creation and team assignment
- Task creation, assignment, status updates, and overdue tracking
- Dashboard with project/task metrics and status breakdown
- Admin-only team role management

## Seeded Admin User

- Email: `admin@owndeck.com`
- Password: `Admin@123`

## Run Backend

```bash
cd backend
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`.

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

H2 console:

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/taskmanager`
- Username: `sa`
- Password: empty

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`.

During local development, Vite proxies `/api` requests to `http://localhost:8080`, so the browser does not need a cross-origin request to reach the Spring Boot app. To point the frontend at a different backend, set `VITE_API_BASE_URL`.

## Validation Notes

- Passwords require at least 8 characters with letters and numbers.
- New signups are created as `ROLE_MEMBER` by default.
- Only authenticated users can access projects, tasks, and dashboard APIs.
- Only admins can change user roles.
- Tasks can only be assigned to project members, project owners, or admins.

## Project Structure

- `backend/`: Spring Boot REST API and SQL data model
- `frontend/`: React app for authentication, dashboard, projects, and tasks

## Assumption

I treated your stack request as `Spring Boot backend + SQL database + React frontend`. The `py` part looked ambiguous, so I did not add a Python service that would duplicate backend responsibility.


Deployed - https://productmanagement-tasks.netlify.app/
