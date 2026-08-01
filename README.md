# InitiativesTracker

A Spring Boot REST API for tracking organisational initiatives, budgets, costs, and savings.

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ (or use `./mvnw`) |
| PostgreSQL | 14+ |

## Environment Variables

The application requires the following environment variables to be set before starting. **No secrets are hard-coded.**

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | Full JDBC connection URL | `jdbc:postgresql://localhost:5432/initiatives_tracker` |
| `DB_USER` | Database username | `tracker_user` |
| `DB_PASS` | Database password | `s3cr3t` |
| `JWT_SECRET` | Secret key for signing JWTs (min 32 chars recommended) | `change-me-in-production` |
| `SERVER_PORT` | HTTP port (optional, defaults to `3014`) | `3014` |
| `JWT_EXPIRATION_MS` | Token lifetime in ms (optional, defaults to `86400000`) | `86400000` |

### Running locally (PowerShell)

```powershell
$env:DB_URL      = "jdbc:postgresql://localhost:5432/initiatives_tracker"
$env:DB_USER     = "tracker_user"
$env:DB_PASS     = "s3cr3t"
$env:JWT_SECRET  = "change-me-to-a-secure-secret-at-least-32-chars"

.\mvnw.cmd spring-boot:run
```

### Running locally (Bash / Linux / macOS)

```bash
export DB_URL="jdbc:postgresql://localhost:5432/initiatives_tracker"
export DB_USER="tracker_user"
export DB_PASS="s3cr3t"
export JWT_SECRET="change-me-to-a-secure-secret-at-least-32-chars"

./mvnw spring-boot:run
```

## Running Flyway Migrations

Flyway is configured to run automatically on startup (`spring.flyway.enabled: true`). Migration scripts live in `src/main/resources/db/migration` and follow the naming convention `V{version}__{description}.sql`.

### Apply migrations manually (without starting the app)

```bash
# Using the Flyway Maven plugin — set env vars first, then:
./mvnw flyway:migrate \
  -Dflyway.url="${DB_URL}" \
  -Dflyway.user="${DB_USER}" \
  -Dflyway.password="${DB_PASS}"
```

```powershell
# PowerShell equivalent
.\mvnw.cmd flyway:migrate `
  "-Dflyway.url=$env:DB_URL" `
  "-Dflyway.user=$env:DB_USER" `
  "-Dflyway.password=$env:DB_PASS"
```

### Check migration status

```bash
./mvnw flyway:info \
  -Dflyway.url="${DB_URL}" \
  -Dflyway.user="${DB_USER}" \
  -Dflyway.password="${DB_PASS}"
```

## Building

```bash
# Skip tests (no database required)
./mvnw -q package -DskipTests

# Full build including tests
./mvnw -q package
```

The WAR artifact is produced at `target/InitiativesTracker-0.0.1-SNAPSHOT.war`.

## API Documentation

Once the application is running, Swagger UI is available at:

```
http://localhost:3014/tracker/swagger-ui.html
```

OpenAPI JSON is at:

```
http://localhost:3014/tracker/api-docs
```