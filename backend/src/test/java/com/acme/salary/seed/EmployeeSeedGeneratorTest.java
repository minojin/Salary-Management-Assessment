package com.acme.salary.seed;

import com.acme.salary.employee.Country;
import com.acme.salary.employee.Employee;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeSeedGeneratorTest {

    private static List<Employee> employees;

    @BeforeAll
    static void generateOnce() {
        employees = new EmployeeSeedGenerator(42).generate(10_000);
    }

    @Test
    void generatesExactlyTheRequestedNumber() {
        assertThat(employees).hasSize(10_000);
        assertThat(new EmployeeSeedGenerator(1).generate(0)).isEmpty();
    }

    @Test
    void sameSeedProducesIdenticalData() {
        List<Employee> again = new EmployeeSeedGenerator(42).generate(100);

        for (int i = 0; i < again.size(); i++) {
            Employee a = employees.get(i);
            Employee b = again.get(i);
            assertThat(b.getEmail()).isEqualTo(a.getEmail());
            assertThat(b.getAnnualSalary()).isEqualTo(a.getAnnualSalary());
            assertThat(b.getCountry()).isEqualTo(a.getCountry());
            assertThat(b.getHireDate()).isEqualTo(a.getHireDate());
        }
    }

    @Test
    void emailsAreUnique() {
        assertThat(employees).extracting(Employee::getEmail).doesNotHaveDuplicates();
    }

    @Test
    void everyCountryIsRepresentedAndIndiaIsTheLargest() {
        Map<Country, Long> byCountry = employees.stream()
                .collect(Collectors.groupingBy(Employee::getCountry, Collectors.counting()));

        assertThat(byCountry.keySet()).containsExactlyInAnyOrder(Country.values());
        Country largest = byCountry.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
        assertThat(largest).isEqualTo(Country.IN);
    }

    @Test
    void salariesFallWithinTheBandForTheirCountryAndJob() {
        for (Employee e : employees) {
            Map.Entry<Long, Long> band = EmployeeSeedGenerator.bandFor(e.getCountry(), e.getJobTitle());
            // Rounding to the nearest 1,000 can move a value up to 500 outside the raw band.
            assertThat(e.getAnnualSalary())
                    .as("%s %s in %s", e.getFullName(), e.getJobTitle(), e.getCountry())
                    .isBetween(band.getKey() - 500, band.getValue() + 500);
            assertThat(e.getAnnualSalary() % 1_000).isZero();
        }
    }

    @Test
    void hireDatesAreInTheConfiguredRange() {
        assertThat(employees).allSatisfy(e -> assertThat(e.getHireDate())
                .isBetween(EmployeeSeedGenerator.EARLIEST_HIRE, EmployeeSeedGenerator.LATEST_HIRE));
    }

    @Test
    void seniorRolesArePaidMoreThanJuniorRolesOnAverage() {
        Map<String, Double> averageInIndia = employees.stream()
                .filter(e -> e.getCountry() == Country.IN)
                .collect(Collectors.groupingBy(Employee::getJobTitle, Collectors.averagingLong(Employee::getAnnualSalary)));

        assertThat(averageInIndia.get("Senior Software Engineer")).isGreaterThan(averageInIndia.get("Software Engineer"));
        assertThat(averageInIndia.get("Software Engineer")).isGreaterThan(averageInIndia.get("Support Specialist"));
    }

    @Test
    void usesSeveralDepartmentsAndJobTitles() {
        Function<Function<Employee, String>, Long> distinct = f -> employees.stream().map(f).distinct().count();

        assertThat(distinct.apply(Employee::getDepartment)).isEqualTo(8);
        assertThat(distinct.apply(Employee::getJobTitle)).isEqualTo(17);
    }
}
