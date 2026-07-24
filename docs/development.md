# Development

## Prerequisites

- Docker with Docker Compose
- Java 25 and Maven 3.9+ for backend development
- Node.js 22 and npm for frontend development

## Environment configuration

Copy the safe development template:

```bash
cp .env.example .env
```

`.env` is ignored by Git. Change its database values only for local development and never put real OAuth credentials or production secrets in it. Backend configuration can also be supplied with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. See the main [README](../README.md#configuration) for all supported variables.

## Run PostgreSQL

```bash
docker compose up -d postgres
```

PostgreSQL listens on `localhost:5432`. Flyway applies migrations when the backend starts.

## Run the backend

Against PostgreSQL:

```bash
cd backend
mvn spring-boot:run
```

With the in-memory H2 local profile:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The API listens on `localhost:8080`. Swagger UI is at <http://localhost:8080/api/docs>.

To debug, launch `dev.codelitz.context.servicecatalog.ContextApplication` from the IDE with the desired Spring profile and environment variables. Do not commit IDE run configurations containing credentials.

## Run the frontend

```bash
cd frontend
npm ci
npm run dev
```

Vite listens on `localhost:5173` and proxies `/api` to `localhost:8080`.

## Run through Docker Compose

```bash
docker compose up --build
```

Use `docker compose logs -f backend` or `docker compose logs -f frontend` to inspect sanitized application logs. Stop containers with `docker compose down`.

## Tests and builds

```bash
cd backend
mvn test
```

The local-profile integration tests always run. The PostgreSQL Testcontainers test runs when Docker is available and skips when it is not.

```bash
cd frontend
npm ci
npm test
npm run build
```

There is currently no separate lint or formatting script. `npm run build` includes TypeScript checking. Maven compilation and tests provide backend validation.

To run Maven tests in a container:

```bash
docker compose --profile test run --rm backend-tests
```

Validate Compose configuration:

```bash
docker compose config --quiet
```

## Common setup problems

- **Port already in use:** stop the process using ports `5432`, `8080`, or `5173`, or stop an older Compose stack.
- **Backend cannot reach PostgreSQL:** wait for `docker compose ps` to report PostgreSQL healthy and verify the `DB_*` or `POSTGRES_*` values.
- **Flyway validation fails:** do not edit an applied migration. Restore it and add a new migration for the change.
- **Testcontainers test is skipped:** start Docker. The H2-backed integration suite still runs without it.
- **OAuth startup fails:** the `oauth` profile requires both GitHub OAuth variables. Use the default or `local` profile when authentication is not under test.
- **Frontend receives API errors:** confirm the backend is running on port `8080`; the Vite proxy targets that port.
- **Stale dependencies or output:** use `npm ci` to restore the lockfile-defined frontend dependencies and `mvn clean test` for a clean backend verification.
