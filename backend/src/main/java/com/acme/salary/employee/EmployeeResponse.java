package com.acme.salary.employee;

import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String fullName,
        String email,
        String jobTitle,
        String department,
        Country country,
        String currency,
        long annualSalary,
        LocalDate hireDate
) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getJobTitle(),
                employee.getDepartment(),
                employee.getCountry(),
                employee.getCountry().currency(),
                employee.getAnnualSalary(),
                employee.getHireDate());
    }
}
