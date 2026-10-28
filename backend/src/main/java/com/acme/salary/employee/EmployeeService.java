package com.acme.salary.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public Page<EmployeeResponse> search(EmployeeSearchCriteria criteria, Pageable pageable) {
        return repository.findAll(EmployeeSpecifications.matching(criteria), pageable).map(EmployeeResponse::from);
    }

    public EmployeeResponse get(Long id) {
        return EmployeeResponse.from(find(id));
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        String email = normaliseEmail(request.email());
        if (repository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
        Employee employee = new Employee(request.fullName().trim(), email, request.jobTitle().trim(),
                request.department().trim(), request.country(), request.annualSalary(), request.hireDate());
        return EmployeeResponse.from(repository.save(employee));
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = find(id);
        String email = normaliseEmail(request.email());
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateEmailException(email);
        }
        employee.update(request.fullName().trim(), email, request.jobTitle().trim(),
                request.department().trim(), request.country(), request.annualSalary(), request.hireDate());
        return EmployeeResponse.from(employee);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Employee find(Long id) {
        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private static String normaliseEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
