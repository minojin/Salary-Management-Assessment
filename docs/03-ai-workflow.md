# How AI Was Used

I built this with **Claude Code** (an agentic coding tool working in my terminal and repository) as a
pair programmer. I made the product and design decisions. The AI did most of the typing, and every
step was checked by running the code: tests, the real app, `curl`, and browser screenshots.

## Workflow

1. **Requirements first.** I gave the assessment brief and asked for a one-page requirements doc
   before any code. I reviewed the scope, and especially *what to leave out and why*. The biggest
   decision came from this step: report salaries **per country in local currency** instead of
   converting with FX rates.
2. **Plan in slices.** I asked for a plan of thin vertical slices
   ([02-plan.md](02-plan.md)). Each slice ended with green tests and a commit, which is why the git
   history reads as the build order.
3. **Build a slice, then verify it.** For each slice: write the code and tests, run them, read the
   failures, and fix the cause (never the test, unless the test itself was wrong). After the backend
   was done, the real jar was run on the seeded database and timed with `curl`. The UI was checked
   with headless-Chrome screenshots at desktop and narrow widths.
4. **Review for simplicity.** After each slice I looked for over-engineering and removed it, for
   example a needless `Function` indirection in the insights service and a meaningless filler assertion.

## Example prompts

> Read the brief. Before writing code, write a one-page requirements doc: goal, persona, in scope,
> deliberately out of scope with the reasoning, and assumptions to confirm with the stakeholder.

> Backend: Spring Boot 3.5, Java 21, SQLite with Flyway. Package by feature (employee, insights, seed,
> common). Countries are an enum that carries the currency. Salary is a whole-unit integer. Return
> RFC 7807 problem details with field errors the UI can show inline. Write service unit tests with
> Mockito, controller slice tests, and repository tests against real SQLite (not H2), so the SQL is
> really tested.

> Insights: aggregate in SQL with GROUP BY, never load 10k rows into Java. Overview by country, and
> per-country breakdown by job title and department. Test the queries with small, hand-checkable numbers.

> Seed: a pure, deterministic generator (fixed seed) kept separate from the runner that writes to
> the DB. Realistic headcount by country, and salary = country base x job multiplier x +/-20%. Test
> determinism, unique emails, and that every salary is inside its band.

> UI: Angular 20 standalone components with signals, and Angular Material. Server-side paging, sort
> and filters with a debounced search. The form mirrors server validation and maps server field
> errors onto inputs. Insights page with a min-avg-max range bar per role.

## Where the AI got it wrong, and what was changed

Verification caught these. They are a good sample of why "trust but verify" matters:

| Problem found | How it was found | Fix |
|---|---|---|
| Maven wrapper compiled with the system **JDK 8**, so records didn't compile | First `./mvnw test` | Point `JAVA_HOME` at JDK 21+. The README states the requirement |
| Hibernate `ddl-auto: validate` rejected a correct SQLite schema (`INTEGER` vs `BIGINT`) | Context failed to start in tests | Flyway owns the schema, and real-SQLite repository tests prove the mapping (see plan) |
| The SQLite dialect raises `JpaSystemException`, not `DataIntegrityViolationException`, on unique violations | Repository test failed | Test changed to assert the DB backstop. The concurrency gap is documented, not hidden |
| `delete(any())` was ambiguous between two Spring Data overloads | Compile error | Typed matcher |
| A test DB shared between test classes could leak seeded rows into other tests | Found in review before it happened | The seed integration test uses its own in-memory database |
| Table headers not right-aligned, and "IndiaINR" missing a space | Screenshot review | Material-specific CSS selectors, explicit margin |
| Insights test failed: `routerLink` needs a router | Frontend test run | `provideRouter([])` in the test |

## What I would not delegate to AI

- **Scope and domain calls**, such as no currency conversion, whole-unit salaries, and email as the
  identifier. These are product decisions with real consequences, so they are written down and listed
  as assumptions to confirm.
- **Deciding a test was wrong instead of the code.** Each such case above was reasoned through, not
  "made green".
