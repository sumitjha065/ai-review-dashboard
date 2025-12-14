# Development (short and simple)

Don't commit secrets (DB passwords, API keys). Keep them local.

Before you start
- Java 17+ installed
- Node.js (v16+) and `npm`
- PostgreSQL (or another DB) if you want to run the backend locally

Quick local setup

1. Copy example config (Windows PowerShell):
```powershell
copy backend\src\main\resources\application.properties.example backend\src\main\resources\application.properties
```

2. Or set environment variables instead (recommended):
- `DATABASE_URL` (JDBC URL), `DB_USER`, `DB_PASS`, `GEMINI_API_KEY`

Run the apps

Start backend (new terminal):
```powershell
cd backend
mvn spring-boot:run
```

Start frontend (another terminal):
```powershell
cd frontend
npm install
npm start
```

One-command dev (optional):
```powershell
npm install
npm run dev
```

Notes
- Keep real secrets out of git. Use local config files or environment variables.
- If you accidentally committed secrets, remove them from the index (`git rm --cached path`) and rotate the keys.

If you want, I can make a small `scripts/` helper or a `docker-compose.yml` to make setup easier.
