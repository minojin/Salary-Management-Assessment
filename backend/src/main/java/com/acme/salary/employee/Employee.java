package com.acme.salary.employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "job_title", nullable = false, length = 80)
    private String jobTitle;

    @Column(nullable = false, length = 80)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "country_code", nullable = false, length = 2)
    private Country country;

    /** Gross annual base salary in whole units of the country's currency. */
    @Column(name = "annual_salary", nullable = false)
    private long annualSalary;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Employee() {
        // for JPA
    }

    public Employee(String fullName, String email, String jobTitle, String department,
                    Country country, long annualSalary, LocalDate hireDate) {
        update(fullName, email, jobTitle, department, country, annualSalary, hireDate);
    }

    public final void update(String fullName, String email, String jobTitle, String department,
                             Country country, long annualSalary, LocalDate hireDate) {
        this.fullName = fullName;
        this.email = email;
        this.jobTitle = jobTitle;
        this.department = department;
        this.country = country;
        this.annualSalary = annualSalary;
        this.hireDate = hireDate;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public Country getCountry() {
        return country;
    }

    public long getAnnualSalary() {
        return annualSalary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
