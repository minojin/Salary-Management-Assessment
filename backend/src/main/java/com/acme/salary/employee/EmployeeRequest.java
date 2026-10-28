package com.acme.salary.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Payload for creating or replacing an employee record. */
public record EmployeeRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 80) String jobTitle,
        @NotBlank @Size(max = 80) String department,
        @NotNull Country country,
        @NotNull @Positive Long annualSalary,
        @NotNull @PastOrPresent LocalDate hireDate
) {
}
