package com.acme.salary.insights;

import com.acme.salary.employee.Country;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.core.api.Assertions.tuple;

/** Verifies the aggregate SQL against real SQLite with small, hand-checkable numbers. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SalaryInsightsRepositoryTest {

    @Autowired
    EmployeeRepository employees;

    @Autowired
    SalaryInsightsRepository insights;

    private int sequence;

    private Employee employee(String jobTitle, String department, Country country, long salary) {
        sequence++;
        return new Employee("Employee " + sequence, "e" + sequence + "@acme.com", jobTitle, department,
                country, salary, LocalDate.of(2020, 1, 1));
    }

    @BeforeEach
    void setUp() {
        employees.saveAllAndFlush(List.of(
                employee("Software Engineer", "Engineering", Country.IN, 1_000_000),
                employee("Software Engineer", "Engineering", Country.IN, 2_000_000),
                employee("Software Engineer", "Engineering", Country.IN, 3_000_001),
                employee("HR Partner", "People", Country.IN, 900_000),
                employee("Software Engineer", "Engineering", Country.US, 150_000),
                employee("Accountant", "Finance", Country.US, 90_000)));
    }

    @Test
    void statsByCountry() {
        List<GroupStats<Country>> rows = insights.statsByCountry();

        assertThat(rows).hasSize(2);
        GroupStats<Country> india = rows.stream().filter(r -> r.key() == Country.IN).findFirst().orElseThrow();
        assertThat(india.headcount()).isEqualTo(4);
        assertThat(india.minSalary()).isEqualTo(900_000);
        assertThat(india.maxSalary()).isEqualTo(3_000_001);
        assertThat(india.averageSalary()).isEqualTo(1_725_000.25);
    }

    @Test
    void statsByJobTitleOnlyIncludesTheGivenCountry() {
        List<GroupStats<String>> rows = insights.statsByJobTitle(Country.IN);

        assertThat(rows).extracting(GroupStats::key).containsExactly("HR Partner", "Software Engineer");
        GroupStats<String> engineers = rows.get(1);
        assertThat(engineers.headcount()).isEqualTo(3);
        assertThat(engineers.minSalary()).isEqualTo(1_000_000);
        assertThat(engineers.maxSalary()).isEqualTo(3_000_001);
        assertThat(engineers.averageSalary()).isEqualTo(2_000_000.333, offset(0.001));
    }

    @Test
    void statsByDepartment() {
        assertThat(insights.statsByDepartment(Country.US))
                .extracting(GroupStats::key, GroupStats::headcount)
                .containsExactly(
                        tuple("Engineering", 1L),
                        tuple("Finance", 1L));
    }

    @Test
    void countryWithNoEmployeesHasNoRows() {
        assertThat(insights.statsByJobTitle(Country.SG)).isEmpty();
    }

    @Test
    void distinctCounts() {
        assertThat(insights.countDistinctDepartments()).isEqualTo(3);
        assertThat(insights.countDistinctJobTitles()).isEqualTo(3);
    }
}
