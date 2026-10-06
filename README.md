# Hostel Management System

A beginner-friendly hostel management mini project with a plain HTML/CSS/JavaScript frontend and a Java Spring Boot/MySQL REST API.

## Start the project

1. Start the MySQL Windows service.
2. Open the project folder and double-click `start-hostel.cmd`.
3. Enter your MySQL password when prompted. It is requested securely and is not saved in the project.
4. The launcher starts the frontend and backend in separate terminal windows, checks that both respond, and opens `http://127.0.0.1:5500/index.html`.

Leave the frontend and backend terminal windows open while using the application. Closing either server window stops that server. Next time, double-click `start-hostel.cmd` again; it reuses already-running servers. If startup fails, keep the launcher open and check the backend terminal for the MySQL error.

The local API runs at `http://localhost:8080`. See `backend/README.md` for database setup and endpoint examples.

## Local demo login

- Student: `26215A0535` or `26215A0536` / `student123`
- Admin: `admin` / `admin123`

These sample accounts are for local practice only. Production disables sample account creation and requires an administrator password configured in Render.

## Public deployment on Render

The included `render.yaml` and `Dockerfile` deploy the frontend and API together over HTTPS. Before creating the Render Blueprint, make sure you have a reachable hosted MySQL database and its JDBC URL, username, and password. Render does not supply this project's MySQL database.

1. Push this project to GitHub and create a Render Blueprint from the repository.
2. Enter the hosted database JDBC URL, database username, and database password as the Blueprint's prompted secret environment values. Never put credentials in source files or commit them.
3. Enter a unique administrator ID and a strong administrator password of at least 14 characters when prompted. Production does not create the local demo accounts.
4. Deploy and open the HTTPS URL Render assigns to the service.
5. After the first successful deployment has created the tables, change the Render environment variable `JPA_DDL_AUTO` from `update` to `validate` and redeploy.

Logins return short-lived signed tokens. Admin APIs require an admin token; student APIs require a student token and only allow that student to access their own records. Render generates the JWT signing secret.

Production does not create local demo students. After deployment, sign in with the configured administrator, then add the student, room, and fee records you want to demonstrate.

This is a learning project, not a production-audited student-record system. Do not store real student personal or financial data in a public demo.