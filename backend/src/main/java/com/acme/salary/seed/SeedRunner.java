package com.acme.salary.seed;

import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Seeds the database on startup. Idempotent: an existing dataset is left alone unless {@code reset} is set. */
@Component
public class SeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);
    private static final int BATCH_SIZE = 1_000;

    private final EmployeeRepository repository;
    private final SeedProperties properties;

    public SeedRunner(EmployeeRepository repository, SeedProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (properties.employeeCount() <= 0) {
            return;
        }
        if (properties.reset()) {
            log.info("Seed reset requested: deleting all employees");
            repository.deleteAllInBatch();
        } else if (repository.count() > 0) {
            log.info("Employees already present; skipping seed");
            return;
        }

        long start = System.nanoTime();
        List<Employee> employees = new EmployeeSeedGenerator(properties.randomSeed())
                .generate(properties.employeeCount());
        for (int from = 0; from < employees.size(); from += BATCH_SIZE) {
            repository.saveAll(employees.subList(from, Math.min(from + BATCH_SIZE, employees.size())));
            repository.flush();
        }
        log.info("Seeded {} employees in {} ms", employees.size(), (System.nanoTime() - start) / 1_000_000);
    }
}
