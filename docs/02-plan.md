# Plan & Design Notes

## Approach

Build in thin vertical slices, test first for domain logic, and commit after each working slice:

1. Requirements and plan (this document)
2. Backend skeleton: Spring Boot + SQLite + Flyway schema + health check
3. Employee CRUD: entity, repository, service, REST controller, validation, error handling, plus tests
4. Search / filter / pagination for the directory, plus tests
5. Salary insights queries (country, job title, department, overview), plus tests
6. Deterministic seed generator for 10,000 employees, plus tests
7. Angular UI: directory, employee form, insights dashboard
8. Packaging: single jar that serves the UI, Dockerfile, README

## Stack

| Layer | Choice | Why |
|-------|--------|-----|
| Backend | Java 21, Spring Boot 3.5, Spring Data JPA, Bean Validation | Matches the role. Mature and well known to reviewers |
| DB | SQLite + Flyway migrations | Suggested by the brief. Zero ops. Flyway keeps the schema versioned and reviewable |
| UI | Angular 20 (standalone components, signals) + Angular Material | Matches the role. Material gives an accessible table, paginator, forms and dialogs |
| Tests | JUnit 5, AssertJ, Mockito, Spring slice tests (`@WebMvcTest`, `@DataJpaTest`) | Fast and focused. No full app boot for unit tests |

## Architecture

```
Angular SPA (Material)                 Spring Boot (one jar)
┌──────────────────────┐   /api/*    ┌───────────────────────────────────────────┐
│ EmployeesPage        │ ──────────▶ │ EmployeeController ─▶ EmployeeService      │
│ EmployeeFormDialog   │             │ InsightsController ─▶ SalaryInsightsService│
│ InsightsPage         │             │ MetaController                             │
│ ApiService (HttpClient)│           │            │                               │
└──────────────────────┘             │   EmployeeRepository (JPA + Specifications) │
        ▲ static files               │            │                               │
        └─────────────────────────── │   SQLite (Flyway V1 schema + indexes)       │
                                     └───────────────────────────────────────────┘
```

Packages are grouped **by feature** (`employee`, `insights`, `seed`, `common`), not by layer, so each
feature is easy to find and change on its own.

## Key decisions & trade-offs

- **Aggregations run in SQL, not in Java.** `GROUP BY` with `MIN/MAX/AVG/COUNT` over indexed columns.
  This stays fast at 10k–100k rows and moves little data. Trade-off: slightly more query code.
- **Countries are an enum (`Country`) with their currency.** This gives one source of truth for
  validation, the UI dropdown and the currency label. Trade-off: adding a country needs a deploy
  (acceptable; this rarely changes).
- **Salary is stored as whole units of local currency (`INTEGER`).** Annual salaries are whole
  numbers in practice. This avoids floating-point problems in SQLite, which has no DECIMAL type.
- **Server-side pagination and filtering** with JPA Specifications. The UI never loads 10k rows.
- **The seed uses a fixed random seed.** The same 10,000 employees every time gives reproducible
  demos and tests. It uses JDBC batch inserts (10k rows in about a second) and runs only when the
  table is empty, so it is idempotent.
- **One deployable.** The Angular build is copied into Spring Boot's static resources: one container,
  no CORS in production. In development, the Angular dev server proxies `/api` to the backend.
- **Errors use RFC 7807 `ProblemDetail`**, with field-level validation messages that the UI shows inline.

## Performance considerations

- Indexes on `country_code`, `department`, `job_title`, `(country_code, job_title)`, and a unique index on `email`.
- Search uses a case-insensitive `LIKE` on name/email. With 10k rows a scan is fine (a few ms).
  At 1M+ rows we would move to SQLite FTS5 or Postgres trigram indexes.
- The page size is capped at 100 on the server to protect the API.
- SQLite runs in WAL mode, which allows concurrent reads while writing. SQLite is right for one team of
  HR users. Moving to Postgres is a config change (JPA plus portable SQL in the migrations).
