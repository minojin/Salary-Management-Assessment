package com.acme.salary.employee;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class EmployeeSpecifications {

    private static final char ESCAPE = '\\';

    private EmployeeSpecifications() {
    }

    static Specification<Employee> matching(EmployeeSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(criteria.search())) {
                String pattern = "%" + escapeLike(criteria.search().trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern, ESCAPE),
                        cb.like(cb.lower(root.get("email")), pattern, ESCAPE)));
            }
            if (criteria.country() != null) {
                predicates.add(cb.equal(root.get("country"), criteria.country()));
            }
            if (hasText(criteria.department())) {
                predicates.add(cb.equal(root.get("department"), criteria.department().trim()));
            }
            if (hasText(criteria.jobTitle())) {
                predicates.add(cb.equal(root.get("jobTitle"), criteria.jobTitle().trim()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /** Treats user input literally, so searching for "50%" does not match everything. */
    static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
