# Hostel Management System Backend

Beginner-friendly REST API built with Java 17, Spring Boot, Spring Data JPA, and MySQL. The frontend in `../frontend` communicates with the API at `http://localhost:8080`.

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

`spring.jpa.hibernate.ddl-auto=update` creates and updates entity tables for development. Do not use this setting with a production database. Hibernate's update mode is not a schema migration tool.

## Run the application

Install Java 17 and Maven. From the `backend` directory, run:

```powershell
mvn spring-boot:run
```

The server runs at `http://localhost:8080`.

For local frontend development, `WebConfig` allows requests from `http://127.0.0.1:5500` and `http://localhost:5500`. Start the frontend on port 5500 and open `http://127.0.0.1:5500/index.html`.

## Demo accounts

- Student IDs `26215A0535` and `26215A0536`; password `student123`.
- Admin ID `admin`; password `admin123`.

Sample passwords are for local practice only. The login endpoint does not issue tokens or protect the other endpoints; do not expose this project publicly or use real accounts.

## API endpoints

Base URL: `http://localhost:8080`

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

Student login request:

```json
{ "studentId": "26215A0535", "password": "student123" }
```

Admin login request:

```json
{ "adminId": "admin", "password": "admin123" }
```

Admin student, room, fee, and leave update URLs use the numeric database IDs returned by their corresponding list endpoints.
