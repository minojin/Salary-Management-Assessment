package com.acme.salary.insights;

import com.acme.salary.employee.Country;
import com.acme.salary.insights.InsightsResponses.CountryBreakdown;
import com.acme.salary.insights.InsightsResponses.CountrySummary;
import com.acme.salary.insights.InsightsResponses.GroupSummary;
import com.acme.salary.insights.InsightsResponses.Overview;
import com.acme.salary.insights.InsightsResponses.SalaryStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InsightsController.class)
class InsightsControllerTest {

    private static final SalaryStats STATS = new SalaryStats(2, 1_000_000, 2_000_000, 1_500_000);
    private static final CountrySummary INDIA = new CountrySummary(Country.IN, "India", "INR", STATS);

    @Autowired
    MockMvc mvc;

    @MockitoBean
    SalaryInsightsService service;

    @Test
    void overview() throws Exception {
        when(service.overview()).thenReturn(new Overview(2, 1, 1, List.of(INDIA)));

        mvc.perform(get("/api/insights/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployees").value(2))
                .andExpect(jsonPath("$.countries[0].currency").value("INR"))
                .andExpect(jsonPath("$.countries[0].stats.averageSalary").value(1_500_000));
    }

    @Test
    void countryBreakdown() throws Exception {
        when(service.countryBreakdown(Country.IN)).thenReturn(new CountryBreakdown(INDIA,
                List.of(new GroupSummary("Software Engineer", STATS)), List.of()));

        mvc.perform(get("/api/insights/countries/IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.countryName").value("India"))
                .andExpect(jsonPath("$.byJobTitle[0].name").value("Software Engineer"))
                .andExpect(jsonPath("$.byJobTitle[0].stats.headcount").value(2));
    }

    @Test
    void unknownCountryReturns400() throws Exception {
        mvc.perform(get("/api/insights/countries/XX")).andExpect(status().isBadRequest());
    }
}
