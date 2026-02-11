# Work Tracker

A lightweight time/activity tracking application with a Java Spring Boot backend, a Vite + React frontend, and a small Python tracker client for activity/idle detection.

## Table of Contents
- Overview
- Architecture
- Tech stack
- Quick start
	- Backend
	- Frontend
	- Tracker client
- Development
- Deployment
- Troubleshooting
- Contributing
- License

## Overview

Work Tracker records activity logs for team members, provides analytics, and includes an optional local tracker client to report activity and idle events.

## Architecture

- Backend: Spring Boot REST API (Maven).
- Frontend: Vite + React single-page app.
- Tracker: Python script that sends activity heartbeats to the backend.
- Dev helpers: `dev-tracker/` contains convenience start scripts for team members.

## Tech stack

- Java, Spring Boot
- Maven
- React, Vite
- Python 3.x for the tracker client
- SQLite / relational DB (see `database-schema.sql`) or other JDBC-compatible DB

## Quick start

Prerequisites
- Java 17+ and Maven for backend (check `backend/pom.xml`).
- Node 16+ and npm/yarn for frontend.
- Python 3.8+ and pip for the tracker client.

Start the backend

```powershell
cd backend
mvn spring-boot:run
```

Or build & run the JAR:

```powershell
cd backend
mvn clean package
java -jar target/*.jar
```

Start the frontend (development)

```bash
cd frontend
npm install
npm run dev
```

The frontend uses Vite — open the printed dev URL (usually http://localhost:5173).

Start the tracker client (local)

```bash
cd tracker
pip install -r requirements.txt
python tracker.py
```

There are convenience start scripts for different users in `dev-tracker/` and the repo root.

## Development

- Backend configuration: `backend/src/main/resources/application.properties` (port, DB settings, API keys).
- Frontend config: `frontend/src/api.js` contains the base API URL used by the UI.
- Tracker config: update endpoint and member identifiers in `tracker/tracker.py` if needed.

Useful commands
- Run backend tests (if present):

```bash
cd backend
mvn test
```

- Build frontend for production:

```bash
cd frontend
npm run build
```

## Deployment

This repo includes simple scripts for deploying and containerizing services.

- To build a Docker image for the backend, check `backend/Dockerfile` and the `deploy` scripts.
- See `DEPLOYMENT.md` and `README-DEPLOYMENT.md` for deployment details and platform-specific notes.

## Troubleshooting

- If the frontend cannot reach the API, check CORS settings in `backend/src/main/java/com/worktracker/config/CorsConfig.java` and the backend `application.properties` port.
- Database errors: confirm the JDBC URL and credentials in `application.properties` and run `database-schema.sql` against the DB.

## Contributing

1. Fork the repo and create a branch for your feature/bugfix.
2. Run unit tests and linting locally.
3. Open a PR with a clear description and linked issue.

## Files of interest

- `backend/` — Spring Boot service, controllers, services, repositories.
- `frontend/` — React + Vite app.
- `tracker/` — Python tracker client; `requirements.txt` lists dependencies.
- `database-schema.sql` — SQL schema used by the backend.

## License

This project is provided under the MIT License — see the `LICENSE` file if present.

---

If you want, I can also:
- add a short `DEVELOPMENT.md` with environment examples, or
- create GitHub Actions to run backend tests and build the frontend automatically.
