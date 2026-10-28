package com.acme.salary.insights;

import com.acme.salary.employee.Country;

import java.util.List;

/** API response shapes for salary insights. Amounts are whole units of the country's currency. */
public final class InsightsResponses {

    private InsightsResponses() {
    }

    public record SalaryStats(long headcount, long minSalary, long maxSalary, long averageSalary) {

        static final SalaryStats EMPTY = new SalaryStats(0, 0, 0, 0);

        static SalaryStats from(GroupStats<?> row) {
            return new SalaryStats(row.headcount(), row.minSalary(), row.maxSalary(),
                    Math.round(row.averageSalary()));
        }
    }

    public record CountrySummary(Country country, String countryName, String currency, SalaryStats stats) {
    }

    public record GroupSummary(String name, SalaryStats stats) {
    }

    public record Overview(long totalEmployees, long departmentCount, long jobTitleCount,
                           List<CountrySummary> countries) {
    }

    public record CountryBreakdown(CountrySummary summary, List<GroupSummary> byJobTitle,
                                   List<GroupSummary> byDepartment) {
    }
}
