# Requirements: ACME Salary Management

## Goal

ACME's HR team manages salary data for ~10,000 employees in several countries using Excel files.
This is slow and error-prone, and it is hard to answer simple questions such as *"what do we pay
a Software Engineer in India?"*. We will replace the spreadsheets with a web application where the
**HR Manager** can:

1. **Manage** employee salary records in one place that is always up to date, and
2. **Understand** how the organisation pays people, without building pivot tables by hand.

Success means the HR Manager can find, add, change or remove any employee's salary record in a few
seconds, and can answer common pay questions from one screen.

## Persona

**HR Manager** at ACME. Knows the domain well and is comfortable with spreadsheets, but is not technical.
Needs speed, accuracy and confidence that the numbers are right.

## In scope

| # | Feature | Why |
|---|---------|-----|
| F1 | **Employee directory**: paginated, sortable list of all employees | Replaces scrolling through a 10k-row sheet |
| F2 | **Search & filter** by name/email, country, department and job title | The most common task is "find this person / this group" |
| F3 | **Add / edit / delete** an employee (name, email, job title, department, country, annual salary, hire date) | Core record keeping; this is the spreadsheet's job today |
| F4 | **Validation**: required fields, valid email, unique email, positive salary, known country, hire date not in the future | Spreadsheets allow bad data; the system should not |
| F5 | **Salary insights by country**: headcount, min / max / average salary | "How do we pay people in country X?" |
| F6 | **Salary insights by job title within a country**: headcount, min / max / average | "What do we pay a *Software Engineer* in *India*?" |
| F7 | **Salary insights by department within a country** | "Which teams cost the most in Germany?" |
| F8 | **Org overview**: total headcount, number of countries / departments / job titles | Context at a glance |
| F9 | **Seed script** that creates 10,000 realistic, deterministic employees | Required for a demo; also lets us test performance |

### Key domain decision: salaries stay in local currency
Each country has a single currency (India → INR, US → USD, …). Salaries are **stored and reported
in the employee's local currency**, and insights are always **grouped by country**. Mixing INR and USD
in one average would give a number that means nothing, and converting would need exchange rates,
which are a separate problem (see *Out of scope*).

## Out of scope (deliberately)

| Left out | Reasoning |
|----------|-----------|
| **Authentication / roles** | There is one persona (HR Manager). Auth is important for production, but it adds a lot of setup and shows little about the core problem. The API is designed so that auth (e.g. Spring Security + SSO) can be added as a filter later without changing the domain code. |
| **Currency conversion / org-wide totals in one currency** | Needs a trusted, dated FX-rate source and a policy (spot rate? budget rate?). Without one, the result would look precise but be wrong. Per-country reporting answers the stated question honestly. |
| **Salary history / audit trail** | Valuable ("when did this person get a raise?"), but it doubles the data model. It is the most likely next feature; the schema can add a `salary_change` table without breaking changes. |
| **Excel / CSV bulk import & export** | Very useful for migrating off spreadsheets, but the seed script already shows bulk loading. Listed as the first follow-up. |
| **Payroll (tax, deductions, payslips)** | A different product. This tool manages *compensation data*, not payments. |
| **Bonus, equity, allowances** | Base annual salary only, to keep the model focused. |
| **Median / percentile bands, charts** | Min / max / average cover the core questions. Percentiles are a good next step (SQLite supports window functions). |
| **Multi-tenant / multiple orgs** | The brief is for one organisation. |

## Non-functional requirements

- **Scale**: 10k employees today, designed for ~100k. The list is paginated on the server, and filter/aggregate columns are indexed.
- **Performance**: list and insights requests should take well under 200 ms on a laptop for 10k rows.
- **Data integrity**: validation on both the server (the source of truth) and the UI (for fast feedback). Email is unique.
- **Quality**: fast, deterministic unit and slice tests for the domain, API and seed logic.
- **Operability**: a single deployable (Spring Boot serves the Angular build), with one Dockerfile.

## Assumptions (to confirm with the stakeholder)

1. Salary means **gross annual base salary**, in the local currency of the employee's work country.
2. An employee belongs to exactly one country, one department and has one job title.
3. Email is a good enough unique business identifier for an employee.
4. Deleting an employee is a hard delete (no soft delete or retention rules yet).
5. The countries list is fixed and managed in code (8 countries for the seed). Adding a country is a code change.
