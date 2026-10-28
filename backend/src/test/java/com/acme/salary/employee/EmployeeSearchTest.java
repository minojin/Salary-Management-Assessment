package com.acme.salary.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.acme.salary.employee.EmployeeFixtures.employee;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EmployeeSearchTest {

    @Autowired
    EmployeeRepository repository;

    @BeforeEach
    void setUp() {
        repository.saveAllAndFlush(List.of(
                employee("Asha Rao", "asha@acme.com", "Software Engineer", "Engineering", Country.IN, 1_800_000),
                employee("Ravi Kumar", "ravi@acme.com", "Software Engineer", "Engineering", Country.IN, 2_400_000),
                employee("Meera Iyer", "meera@acme.com", "HR Partner", "People", Country.IN, 1_200_000),
                employee("John Smith", "john.smith@acme.com", "Software Engineer", "Engineering", Country.US, 140_000),
                employee("Jane 100% Doe", "jane_doe@acme.com", "Accountant", "Finance", Country.US, 95_000)));
    }

    private List<String> names(EmployeeSearchCriteria criteria) {
        return repository.findAll(EmployeeSpecifications.matching(criteria), Sort.by("fullName"))
                .stream().map(Employee::getFullName).toList();
    }

    @Test
    void noCriteriaReturnsEveryone() {
        assertThat(names(EmployeeSearchCriteria.none())).hasSize(5);
    }

    @Test
    void searchMatchesNameOrEmailCaseInsensitively() {
        assertThat(names(new EmployeeSearchCriteria("RAO", null, null, null))).containsExactly("Asha Rao");
        assertThat(names(new EmployeeSearchCriteria("john.smith@", null, null, null))).containsExactly("John Smith");
    }

    @Test
    void searchTreatsLikeWildcardsLiterally() {
        assertThat(names(new EmployeeSearchCriteria("100%", null, null, null))).containsExactly("Jane 100% Doe");
        assertThat(names(new EmployeeSearchCriteria("e_d", null, null, null))).containsExactly("Jane 100% Doe");
    }

    @Test
    void filtersAreCombinedWithAnd() {
        var criteria = new EmployeeSearchCriteria(null, Country.IN, "Engineering", "Software Engineer");

        assertThat(names(criteria)).containsExactly("Asha Rao", "Ravi Kumar");
    }

    @Test
    void pagesAndSortsBySalary() {
        var page = repository.findAll(EmployeeSpecifications.matching(EmployeeSearchCriteria.none()),
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "annualSalary")));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).extracting(Employee::getFullName).containsExactly("Ravi Kumar", "Asha Rao");
    }

    @Test
    void distinctDepartmentsAndJobTitlesAreSorted() {
        assertThat(repository.findDistinctDepartments()).containsExactly("Engineering", "Finance", "People");
        assertThat(repository.findDistinctJobTitles()).containsExactly("Accountant", "HR Partner", "Software Engineer");
    }
}
