# Hostel Management System

A beginner-friendly hostel management mini project with a plain HTML/CSS/JavaScript frontend and a Java Spring Boot/MySQL REST API. The frontend calls the backend at `http://localhost:8080`.

## Run the backend

1. Start MySQL and make sure the `hostel_management` database exists.
2. Set the `DB_USERNAME` and `DB_PASSWORD` environment variables to your local MySQL credentials. Do not commit credentials to the repository.
3. From PowerShell, set the credentials and run the backend:

   ```powershell
   $env:DB_USERNAME = "root"
   $env:DB_PASSWORD = "your-local-mysql-password"
   cd backend
   mvn spring-boot:run
   ```

The API runs at `http://localhost:8080`. Keep the backend terminal open while using the frontend. See `backend/README.md` for database setup and endpoint examples.

## Run the frontend

In another PowerShell terminal from the repository root, run:

```powershell
cd frontend
npx --yes http-server . -p 5500
```

Open `http://127.0.0.1:5500/index.html`. The backend allows development requests from `http://127.0.0.1:5500` and `http://localhost:5500`.

## Demo login

- Student: `26215A0535` or `26215A0536` / `student123`
- Admin: `admin` / `admin123`

The login endpoint checks the credentials, but this beginner-stage API does not issue a token or protect other endpoints. Do not expose it to the public internet or use it with real accounts.