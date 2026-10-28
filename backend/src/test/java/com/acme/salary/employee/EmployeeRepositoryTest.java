package com.acme.salary.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ActiveProfiles;

import static com.acme.salary.employee.EmployeeFixtures.HIRE_DATE;
import static com.acme.salary.employee.EmployeeFixtures.employee;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Runs against real SQLite (in memory) with the Flyway schema, so mappings and constraints are checked for real. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    EmployeeRepository repository;

    @Test
    void savesAndReadsBackAllFields() {
        Employee saved = repository.saveAndFlush(
                employee("Asha Rao", "asha@acme.com", "Software Engineer", "Engineering", Country.IN, 1_800_000));

        Employee found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getCountry()).isEqualTo(Country.IN);
        assertThat(found.getHireDate()).isEqualTo(HIRE_DATE);
        assertThat(found.getAnnualSalary()).isEqualTo(1_800_000);
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void databaseEnforcesUniqueEmailAsBackstopToServiceCheck() {
        repository.saveAndFlush(employee("A", "same@acme.com", "SE", "Eng", Country.IN, 1));

        assertThatThrownBy(() -> repository.saveAndFlush(employee("B", "same@acme.com", "SE", "Eng", Country.US, 1)))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("UNIQUE");
    }

    @Test
    void existsByEmailAndIdNotIgnoresTheEmployeeItself() {
        Employee asha = repository.saveAndFlush(employee("A", "asha@acme.com", "SE", "Eng", Country.IN, 1));

        assertThat(repository.existsByEmailAndIdNot("asha@acme.com", asha.getId())).isFalse();
        assertThat(repository.existsByEmailAndIdNot("asha@acme.com", asha.getId() + 1)).isTrue();
    }
}
