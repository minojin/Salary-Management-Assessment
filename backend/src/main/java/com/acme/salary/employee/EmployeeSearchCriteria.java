package com.acme.salary.employee;

/**
 * Optional filters for the employee directory. Every filter that is set must match (AND).
 *
 * @param search     case-insensitive substring of name or email
 * @param country    exact country
 * @param department exact department
 * @param jobTitle   exact job title
 */
public record EmployeeSearchCriteria(String search, Country country, String department, String jobTitle) {

    public static EmployeeSearchCriteria none() {
        return new EmployeeSearchCriteria(null, null, null, null);
    }
}
