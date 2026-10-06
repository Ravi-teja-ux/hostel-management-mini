# Hostel Management System Backend

Beginner-friendly REST API built with Java 17, Spring Boot, Spring Data JPA, and MySQL. In production, Spring Boot serves the frontend from the same HTTPS origin.

## MySQL setup

1. Start MySQL Server.
2. Create the database:

   ```sql
   CREATE DATABASE hostel_management;
   ```

3. Set database credentials as environment variables before starting the backend. In PowerShell:

   ```powershell
   $env:DB_USERNAME = "root"
   $env:DB_PASSWORD = "your-local-mysql-password"
   ```

   Credentials are not stored in the repository. `application.properties` reads these variables; the username defaults to `root` and the password defaults to empty if not set.

   From the project root, `start-hostel.cmd` prompts for the password without displaying or saving it and starts both servers.

`spring.jpa.hibernate.ddl-auto=update` creates and updates entity tables for development. Do not use this setting with a production database. Hibernate's update mode is not a schema migration tool.

## Run the application

Install Java 17 and Maven. From the `backend` directory, run:

```powershell
mvn spring-boot:run
```

The server runs at `http://localhost:8080`.

For local frontend development, `WebConfig` allows requests from `http://127.0.0.1:5500` and `http://localhost:5500`. Start the frontend on port 5500 and open `http://127.0.0.1:5500/index.html`.

## Demo accounts

- Local student IDs `26215A0535` and `26215A0536`; password `student123`.
- Local admin ID `admin`; password `admin123`.

Sample passwords are for local practice only. Login returns a short-lived signed token. Student endpoints require a student token and enforce ownership; admin endpoints require an admin token.

## Deploy on Render

The repository's `render.yaml` and root `Dockerfile` deploy the frontend and API as one HTTPS service. Provide a hosted MySQL JDBC URL, `DB_USERNAME`, and `DB_PASSWORD` as Render environment secrets. Use a database user scoped to this application's database rather than the MySQL root user; a database running only on your computer is not reachable by Render.

Set `ADMIN_INITIAL_ID` and a strong `ADMIN_INITIAL_PASSWORD` of at least 14 characters in Render. Production disables sample account creation and provisions this administrator securely. Render generates `JWT_SECRET`; do not replace it with a committed value.

Production starts without the local demo student or admin accounts. Sign in with the configured administrator to create demonstration records.

The Blueprint uses `JPA_DDL_AUTO=update` for the first startup so JPA can create the tables. After the first successful deployment, change it to `validate` in Render and redeploy. `update` is only for initial development setup, not ongoing production use.

## API endpoints

Local base URL: `http://localhost:8080`. In production the API shares the Render HTTPS origin with the frontend.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/student/login` | Student login |
| `GET` | `/api/students/{studentId}` | Student profile |
| `GET` | `/api/students/{studentId}/room` | Student room assignment |
| `GET` | `/api/students/{studentId}/fees` | Student fee record |
| `POST` | `/api/leave-requests` | Submit a leave request |
| `POST` | `/api/admin/login` | Admin login |
| `GET` | `/api/admin/dashboard` | Summary counts |
| `GET`, `POST` | `/api/admin/students` | List or add students |
| `PUT`, `DELETE` | `/api/admin/students/{id}` | Update or delete a student |
| `GET`, `POST` | `/api/admin/rooms` | List or add rooms |
| `PUT`, `DELETE` | `/api/admin/rooms/{id}` | Update or delete a room |
| `GET` | `/api/admin/fees` | List fee records |
| `PUT` | `/api/admin/fees/{id}` | Update a fee record |
| `GET` | `/api/admin/leave-requests` | List leave requests |
| `PUT` | `/api/admin/leave-requests/{id}/approve` | Approve a leave request |
| `PUT` | `/api/admin/leave-requests/{id}/reject` | Reject a leave request |

Student login request (returns a signed access token):

```json
{ "studentId": "26215A0535", "password": "student123" }
```

Admin login request:

```json
{ "adminId": "admin", "password": "admin123" }
```

Admin student, room, fee, and leave update URLs use the numeric database IDs returned by their corresponding list endpoints.
