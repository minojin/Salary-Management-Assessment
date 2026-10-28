package com.acme.salary.seed;

import com.acme.salary.employee.Country;
import com.acme.salary.employee.Employee;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Generates realistic, deterministic employees: the same seed always gives the same people.
 * Pure logic with no I/O, so it is unit-testable and reusable.
 *
 * <p>Salary = country base (local currency, for a mid-level engineer) x job-level multiplier x random spread
 * of +/-20%, rounded to the nearest 1,000.
 */
public class EmployeeSeedGenerator {

    static final LocalDate EARLIEST_HIRE = LocalDate.of(2010, 1, 1);
    static final LocalDate LATEST_HIRE = LocalDate.of(2025, 12, 31);
    static final double SPREAD = 0.20;

    /** Mid-level engineer salary in local currency, and the share of the workforce in that country (%). */
    private record CountryProfile(Country country, long baseSalary, int weight) {
    }

    private record Job(String department, String title, double multiplier, int weight) {
    }

    private static final List<CountryProfile> COUNTRIES = List.of(
            new CountryProfile(Country.IN, 1_800_000, 40),
            new CountryProfile(Country.US, 130_000, 20),
            new CountryProfile(Country.GB, 70_000, 10),
            new CountryProfile(Country.DE, 72_000, 8),
            new CountryProfile(Country.FR, 58_000, 6),
            new CountryProfile(Country.CA, 100_000, 6),
            new CountryProfile(Country.AU, 120_000, 5),
            new CountryProfile(Country.SG, 96_000, 5));

    private static final List<Job> JOBS = List.of(
            new Job("Engineering", "Software Engineer", 1.00, 22),
            new Job("Engineering", "Senior Software Engineer", 1.40, 12),
            new Job("Engineering", "Engineering Manager", 1.90, 3),
            new Job("Engineering", "QA Engineer", 0.80, 7),
            new Job("Product", "Product Manager", 1.50, 4),
            new Job("Product", "Product Designer", 0.95, 3),
            new Job("Data", "Data Analyst", 0.85, 5),
            new Job("Data", "Data Scientist", 1.30, 3),
            new Job("People", "HR Partner", 0.75, 3),
            new Job("People", "Recruiter", 0.65, 3),
            new Job("Finance", "Accountant", 0.70, 3),
            new Job("Finance", "Financial Analyst", 0.85, 2),
            new Job("Sales", "Sales Executive", 0.70, 8),
            new Job("Sales", "Account Manager", 0.90, 5),
            new Job("Customer Support", "Support Specialist", 0.50, 10),
            new Job("Customer Support", "Support Lead", 0.75, 3),
            new Job("Marketing", "Marketing Specialist", 0.75, 4));

    private static final List<String> FIRST_NAMES = List.of(
            "Aarav", "Aditi", "Akash", "Ananya", "Arjun", "Divya", "Ishaan", "Kavya", "Meera", "Neha",
            "Priya", "Rahul", "Rohan", "Sanjay", "Sneha", "Vikram", "James", "Mary", "John", "Patricia",
            "Robert", "Jennifer", "Michael", "Linda", "David", "Elizabeth", "William", "Sarah", "Oliver",
            "Amelia", "Harry", "Isla", "George", "Emily", "Lukas", "Hannah", "Felix", "Lena", "Jonas",
            "Sophie", "Louis", "Chloe", "Hugo", "Camille", "Liam", "Olivia", "Noah", "Emma", "Jack",
            "Charlotte", "Wei", "Mei", "Jun", "Hui", "Ethan", "Ava", "Lucas", "Mia", "Mateo", "Zara");

    private static final List<String> LAST_NAMES = List.of(
            "Sharma", "Patel", "Iyer", "Reddy", "Nair", "Gupta", "Singh", "Rao", "Menon", "Das",
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Wilson",
            "Taylor", "Evans", "Thomas", "Roberts", "Walker", "Muller", "Schmidt", "Schneider", "Fischer",
            "Weber", "Martin", "Bernard", "Dubois", "Moreau", "Laurent", "Tremblay", "Roy", "Gagnon",
            "Lee", "Tan", "Lim", "Ng", "Wong", "Chen", "Kelly", "Murphy", "OConnor", "Clarke", "Young");

    private final Random random;

    public EmployeeSeedGenerator(long seed) {
        this.random = new Random(seed);
    }

    public List<Employee> generate(int count) {
        List<Employee> employees = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            employees.add(next(i));
        }
        return employees;
    }

    private Employee next(int sequence) {
        String first = pick(FIRST_NAMES);
        String last = pick(LAST_NAMES);
        CountryProfile country = pickWeighted(COUNTRIES, COUNTRIES.stream().mapToInt(CountryProfile::weight).toArray());
        Job job = pickWeighted(JOBS, JOBS.stream().mapToInt(Job::weight).toArray());
        // The sequence number keeps emails unique even when two people share a name.
        String email = (first + "." + last + "." + sequence + "@acme.com").toLowerCase(Locale.ROOT);
        return new Employee(first + " " + last, email, job.title(), job.department(), country.country(),
                salary(country, job), hireDate());
    }

    private long salary(CountryProfile country, Job job) {
        double spread = 1 + (random.nextDouble() * 2 - 1) * SPREAD;
        long raw = Math.round(country.baseSalary() * job.multiplier() * spread);
        return Math.max(1_000, Math.round(raw / 1_000.0) * 1_000);
    }

    private LocalDate hireDate() {
        long days = ChronoUnit.DAYS.between(EARLIEST_HIRE, LATEST_HIRE);
        return EARLIEST_HIRE.plusDays(random.nextLong(days + 1));
    }

    private <T> T pick(List<T> values) {
        return values.get(random.nextInt(values.size()));
    }

    private <T> T pickWeighted(List<T> values, int[] weights) {
        int total = 0;
        for (int weight : weights) {
            total += weight;
        }
        int roll = random.nextInt(total);
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return values.get(i);
            }
        }
        throw new IllegalStateException("unreachable");
    }

    /** Exposed for tests: the salary band a job can fall in for a country, before rounding. */
    static Map.Entry<Long, Long> bandFor(Country country, String jobTitle) {
        CountryProfile profile = COUNTRIES.stream().filter(c -> c.country() == country).findFirst().orElseThrow();
        Job job = JOBS.stream().filter(j -> j.title().equals(jobTitle)).findFirst().orElseThrow();
        double mid = profile.baseSalary() * job.multiplier();
        return Map.entry(Math.round(mid * (1 - SPREAD)), Math.round(mid * (1 + SPREAD)));
    }
}
