package com.acme.salary.insights;

import com.acme.salary.employee.Country;
import com.acme.salary.insights.InsightsResponses.CountryBreakdown;
import com.acme.salary.insights.InsightsResponses.Overview;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final SalaryInsightsService service;

    public InsightsController(SalaryInsightsService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public Overview overview() {
        return service.overview();
    }

    @GetMapping("/countries/{country}")
    public CountryBreakdown country(@PathVariable Country country) {
        return service.countryBreakdown(country);
    }
}
