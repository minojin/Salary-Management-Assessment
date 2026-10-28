package com.acme.salary.employee;

import java.time.LocalDate;

final class EmployeeFixtures {

    static final LocalDate HIRE_DATE = LocalDate.of(2021, 4, 1);

    private EmployeeFixtures() {
    }

    static EmployeeRequest request() {
        return new EmployeeRequest("Asha Rao", "Asha.Rao@acme.com ", "Software Engineer", "Engineering",
                Country.IN, 1_800_000L, HIRE_DATE);
    }

    static Employee employee(String name, String email, String jobTitle, String department,
                             Country country, long salary) {
        return new Employee(name, email, jobTitle, department, country, salary, HIRE_DATE);
    }
}
