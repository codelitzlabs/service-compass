# Contributing to Service Compass

Thank you for helping make engineering context easier to find.

## Principles

- AI may accelerate development, but engineers own decisions and review the result.
- Prefer quality, clarity, and maintainability over speed.
- Keep architecture and boundaries explicit without premature abstractions.
- Keep integrations isolated from catalog business logic.
- Preserve existing behavior unless a change is intentional and documented.

## Workflow

1. For substantial changes, open an issue describing the user problem and intended scope.
2. Fork the repository and create a branch from the default branch.
3. Use a descriptive branch name such as `feature/add-service-search`, `fix/github-import-error`, or `docs/improve-local-setup`.
4. Make a focused change, add or update tests, and update affected documentation.
5. Run the relevant validation described below.
6. Open a pull request using the repository template.

Keep pull requests reasonably small and focused. Use concise, meaningful commit messages that explain the change.

## Local setup and tests

Start the complete application:

```bash
cp .env.example .env
docker compose up --build
```

Run backend validation:

```bash
cd backend
mvn test
```

Run frontend validation:

```bash
cd frontend
npm ci
npm test
npm run build
```

See [docs/development.md](docs/development.md) for manual startup, configuration, and debugging.

## Code quality and architecture

- Backend business rules belong in services, input validation in DTOs/controllers, and persistence in repositories/entities.
- Frontend server state belongs in TanStack Query; API calls stay in `frontend/src/lib.ts`.
- Add a new Flyway migration for schema changes. Never edit an applied migration.
- Avoid coupling the core catalog to external providers.
- Match existing naming and style. Do not add an abstraction until it solves a current problem.
- Add tests for changed behavior and document user-visible or operational changes.

## Pull requests

Explain the problem, approach, tradeoffs, and testing performed. Include sanitized screenshots for interface changes. Call out migrations, compatibility concerns, security implications, and follow-up work. Maintainers may ask for a narrower scope or additional tests.

## Bugs and feature proposals

Use the GitHub issue templates. Search existing issues first and provide a minimal reproduction for bugs. Feature proposals should focus on the problem and fit with Service Compass as a small engineering context platform.

For security vulnerabilities, follow [SECURITY.md](SECURITY.md) instead of opening a public issue.

## Sensitive information

Never commit secrets, tokens, credentials, customer data, confidential information, internal URLs, production configuration, or unsanitized logs and screenshots. Use fictional `example.com` data in tests and examples.
