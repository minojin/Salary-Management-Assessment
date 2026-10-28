package com.acme.salary.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param employeeCount how many employees to create; 0 disables seeding
 * @param reset         delete all employees first (used by the seed script); otherwise seed only an empty table
 * @param randomSeed    fixed seed so every run produces the same data
 */
@ConfigurationProperties("app.seed")
public record SeedProperties(
        @DefaultValue("0") int employeeCount,
        @DefaultValue("false") boolean reset,
        @DefaultValue("42") long randomSeed) {
}
