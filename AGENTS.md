# AGENTS.md

## Project

- `backend/`: Spring Boot API. Keep business rules in services, validation in DTOs/controllers, and persistence in repositories/entities.
- `frontend/`: React + TypeScript. Use TanStack Query for server state and keep API calls in `src/lib.ts`.
- PostgreSQL is the source of truth. Add new Flyway migrations for schema changes; never modify an applied migration.

## Working rules

- Keep changes focused, explicit, and consistent with the existing code.
- Preserve existing data and API behavior unless the task requires a breaking change.
- Add or update tests for changed behavior.
- Do not overwrite unrelated local changes.

## Verification

```bash
cd backend && mvn test
cd frontend && npm test && npm run build
```

Use `docker compose up --build` for full-stack verification when needed.
