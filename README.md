# neustudy-admin

NEU Study Admin monorepo.

## Structure

- `frontend/` - Vue 3 + Vite admin frontend
- `backend/` - Spring Boot admin backend

## Local Configuration

- Frontend: copy `frontend/.env.example` to `frontend/.env.local` or another local env file.
- Backend: copy `backend/src/main/resources/application-local.example.yml` to `backend/src/main/resources/application-local.yml`.

Local env files are intentionally ignored by Git.
