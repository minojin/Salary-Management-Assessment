package com.acme.salary.insights;

/**
 * Raw aggregate row produced by the database for one group (a country, job title or department).
 * Boxed types match what JPQL aggregate functions return.
 */
public record GroupStats<K>(K key, Long headcount, Long minSalary, Long maxSalary, Double averageSalary) {
}
