package com.acme.salary.employee;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long id) {
        super("Employee " + id + " not found");
    }
}
