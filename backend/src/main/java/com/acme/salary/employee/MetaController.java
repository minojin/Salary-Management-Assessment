package com.acme.salary.employee;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** Reference data for UI dropdowns: the supported countries and the departments / job titles in use. */
@RestController
public class MetaController {

    private final EmployeeRepository repository;

    public MetaController(EmployeeRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/meta")
    public Meta meta() {
        List<CountryOption> countries = Arrays.stream(Country.values())
                .map(c -> new CountryOption(c, c.displayName(), c.currency()))
                .toList();
        return new Meta(countries, repository.findDistinctDepartments(), repository.findDistinctJobTitles());
    }

    public record CountryOption(Country code, String name, String currency) {
    }

    public record Meta(List<CountryOption> countries, List<String> departments, List<String> jobTitles) {
    }
}
