# Demo Script (≈ 4 minutes)

A walkthrough for the video, told from the HR Manager's point of view.

1. **Problem (15 s):** "ACME's HR team manages 10,000 salaries across 8 countries in Excel. This
   replaces that with one place to manage records and answer pay questions."
2. **Directory (60 s):** Open *Employees*. Show the count (10,000). Search "sharma", then filter
   Country = India and Job title = Software Engineer. Sort by salary, highest first. Page through.
   Point out that the list is served page by page, so it stays fast at any size.
3. **Add (45 s):** *Add employee*. Submit empty to show inline validation. Pick a country and point
   out the currency suffix. Enter a future hire date to show the error. Fill it in correctly and save:
   a snackbar appears and the new person is searchable.
4. **Guardrail (20 s):** Add another employee with the same email. The server's 409 is shown on the
   email field.
5. **Edit and delete (30 s):** Give someone a raise (edit salary), then delete a record with confirmation.
6. **Insights (60 s):** Open *Insights*. Show the KPIs, then countries by headcount with min/avg/max
   in local currency. Explain why currencies are never mixed. Show India by job title, sorted by
   average, and read the range bars ("Engineering Managers earn the most, and Support has the
   narrowest band"). Switch to *By department*. Click the United States.
7. **Engineering (30 s):** Show the commit history, the test runs (`./mvnw test`, `npm run test:ci`),
   and the docs folder.
