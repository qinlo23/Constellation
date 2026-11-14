# Repository Guidelines

## Project Structure & Module Organization

OWL is a multi-module Maven backend built with Java 25, Spring Boot 4.1, and MyBatis-Plus 3.5.17.

- Base modules: `common` (Result, security, mail, config), `api` (shared business models such as `RoleDO`/`RoleMapper`), `infra` (Redis, RabbitMQ, MinIO, Nacos), `log` (operation logs), and `start` (runnable application).
- Business modules live under `watermelon/<name>`: `user`, `administration`, `sugarcane`, `crow`, and `mango`. `pitaya` (电商) is deprecated: paused from development and removed from the Maven build, but its code stays in `.rubbish/pitaya`.
- Java sources are under `src/main/java`, resources under `src/main/resources`, and tests under `src/test/java`.
- Documentation lives in `.docs/`; database DDL and migrations live in `.docs/database/`.

## Build, Test, and Development Commands

```bash
# Full build, skipping tests
mvn clean package -DskipTests

# Test one module and its dependencies
mvn test -pl watermelon/user -am

# Run the application
java -jar start/target/start-0.0.1.jar
```

The default Spring profile is `dev`. Local startup requires MySQL, Redis, RabbitMQ, Nacos, and MinIO. CI runs `mvn -B package -DskipTests` on pushes and pull requests to `main`/`master`.

## Coding Style & Naming Conventions

- Use 4-space indentation, UTF-8 encoding, and Chinese comments with English identifiers.
- Add Javadoc to public controllers, services, and utility APIs.
- Never commit commented-out code; rely on Git history instead.
- Name entities with the `*DO` suffix, place DTOs/VOs under the module `domain` package, and use MapStruct converters.
- Controllers return `Result<T>` from `common`; authenticated user data comes from `CurrentUserContext`.
- Use MyBatis-Plus `LambdaQueryWrapper` / `LambdaUpdateWrapper` for queries.
## Testing Guidelines

- Use JUnit 5 and Spring Boot Test.
- Place tests in `src/test/java` and name classes with the `*Test` suffix.
- Run tests with `mvn test -pl <module> -am`.
- No coverage gate is enforced; add tests for security and core business logic.

## Commit & Pull Request Guidelines

- Follow the existing commit style: concise Chinese summary with numbered items when there are multiple changes, for example `1. 功能A， 2. 修复B`.
- In a pull request, describe the purpose, summarize the changes, list verification commands, and update related docs, DDL, or ADRs when behavior changes.
- Attach screenshots for user-facing changes and link related issues when available.

## Agent-Specific Notes

- Read `.docs/others/新模块开发指南.md` before adding a new `watermelon` module and register it in the root `pom.xml` and `start` module.
- Follow existing ADRs in `.docs/adr/`; add a new ADR for significant architecture decisions.
- REST controllers are automatically prefixed with `/api`; update `SecurityConfig` whitelist rules when exposing public endpoints.
