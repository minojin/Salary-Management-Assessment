package com.acme.salary.seed;

import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.insights.SalaryInsightsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Boots the whole app with seeding on (own in-memory DB, so other tests are not affected). */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite:file:seedtest?mode=memory&cache=shared",
        "app.seed.employee-count=2000"
})
@ActiveProfiles("test")
class SeedIntegrationTest {

    @Autowired
    EmployeeRepository repository;

    @Autowired
    SalaryInsightsService insights;

    @Test
    void seedsOnStartupAndInsightsAddUp() {
        assertThat(repository.count()).isEqualTo(2000);

        var overview = insights.overview();
        assertThat(overview.totalEmployees()).isEqualTo(2000);
        assertThat(overview.countries()).hasSize(8);
        assertThat(overview.departmentCount()).isEqualTo(8);
    }
}
