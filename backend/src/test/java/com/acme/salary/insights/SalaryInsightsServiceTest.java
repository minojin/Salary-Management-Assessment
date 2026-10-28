package com.acme.salary.insights;

import com.acme.salary.employee.Country;
import com.acme.salary.insights.InsightsResponses.CountryBreakdown;
import com.acme.salary.insights.InsightsResponses.Overview;
import com.acme.salary.insights.InsightsResponses.SalaryStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalaryInsightsServiceTest {

    @Mock
    SalaryInsightsRepository repository;

    @InjectMocks
    SalaryInsightsService service;

    @Test
    void overviewSortsCountriesByHeadcountAndSumsTotal() {
        when(repository.statsByCountry()).thenReturn(List.of(
                new GroupStats<>(Country.US, 10L, 50_000L, 200_000L, 120_000.0),
                new GroupStats<>(Country.IN, 30L, 500_000L, 5_000_000L, 1_500_000.0)));
        when(repository.countDistinctDepartments()).thenReturn(5L);
        when(repository.countDistinctJobTitles()).thenReturn(12L);

        Overview overview = service.overview();

        assertThat(overview.totalEmployees()).isEqualTo(40);
        assertThat(overview.departmentCount()).isEqualTo(5);
        assertThat(overview.jobTitleCount()).isEqualTo(12);
        assertThat(overview.countries()).extracting(c -> c.country()).containsExactly(Country.IN, Country.US);
        assertThat(overview.countries().get(0).currency()).isEqualTo("INR");
        assertThat(overview.countries().get(0).countryName()).isEqualTo("India");
    }

    @Test
    void averageIsRoundedToWholeCurrencyUnits() {
        when(repository.statsByCountry()).thenReturn(List.of(
                new GroupStats<>(Country.IN, 3L, 1L, 3L, 2_000_000.5)));
        when(repository.statsByJobTitle(Country.IN)).thenReturn(List.of(
                new GroupStats<>("Software Engineer", 3L, 1L, 3L, 1_999_999.4)));
        when(repository.statsByDepartment(Country.IN)).thenReturn(List.of());

        CountryBreakdown breakdown = service.countryBreakdown(Country.IN);

        assertThat(breakdown.summary().stats().averageSalary()).isEqualTo(2_000_001);
        assertThat(breakdown.byJobTitle().get(0).stats().averageSalary()).isEqualTo(1_999_999);
    }

    @Test
    void countryWithoutEmployeesReturnsZeroStatsInsteadOfFailing() {
        when(repository.statsByCountry()).thenReturn(List.of());
        when(repository.statsByJobTitle(Country.SG)).thenReturn(List.of());
        when(repository.statsByDepartment(Country.SG)).thenReturn(List.of());

        CountryBreakdown breakdown = service.countryBreakdown(Country.SG);

        assertThat(breakdown.summary().stats()).isEqualTo(new SalaryStats(0, 0, 0, 0));
        assertThat(breakdown.summary().currency()).isEqualTo("SGD");
        assertThat(breakdown.byJobTitle()).isEmpty();
    }
}
