package com.acme.salary.insights;

import com.acme.salary.employee.Country;
import com.acme.salary.insights.InsightsResponses.CountryBreakdown;
import com.acme.salary.insights.InsightsResponses.CountrySummary;
import com.acme.salary.insights.InsightsResponses.GroupSummary;
import com.acme.salary.insights.InsightsResponses.Overview;
import com.acme.salary.insights.InsightsResponses.SalaryStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SalaryInsightsService {

    private final SalaryInsightsRepository repository;

    public SalaryInsightsService(SalaryInsightsRepository repository) {
        this.repository = repository;
    }

    /** Org-wide picture: headcount and per-country pay. Countries are sorted by headcount, largest first. */
    public Overview overview() {
        List<CountrySummary> countries = repository.statsByCountry().stream()
                .map(row -> summary(row.key(), SalaryStats.from(row)))
                .sorted(Comparator.comparingLong((CountrySummary c) -> c.stats().headcount()).reversed()
                        .thenComparing(c -> c.country().name()))
                .toList();
        long total = countries.stream().mapToLong(c -> c.stats().headcount()).sum();
        return new Overview(total, repository.countDistinctDepartments(), repository.countDistinctJobTitles(),
                countries);
    }

    /** Pay in one country, broken down by job title and by department. */
    public CountryBreakdown countryBreakdown(Country country) {
        Map<Country, SalaryStats> byCountry = repository.statsByCountry().stream()
                .collect(Collectors.toMap(GroupStats::key, SalaryStats::from));
        SalaryStats stats = byCountry.getOrDefault(country, SalaryStats.EMPTY);
        return new CountryBreakdown(
                summary(country, stats),
                groups(repository.statsByJobTitle(country)),
                groups(repository.statsByDepartment(country)));
    }

    private static CountrySummary summary(Country country, SalaryStats stats) {
        return new CountrySummary(country, country.displayName(), country.currency(), stats);
    }

    private static List<GroupSummary> groups(List<GroupStats<String>> rows) {
        return rows.stream().map(row -> new GroupSummary(row.key(), SalaryStats.from(row))).toList();
    }
}
