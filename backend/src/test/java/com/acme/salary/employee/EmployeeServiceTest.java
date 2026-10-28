package com.acme.salary.employee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.acme.salary.employee.EmployeeFixtures.employee;
import static com.acme.salary.employee.EmployeeFixtures.request;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    EmployeeRepository repository;

    @InjectMocks
    EmployeeService service;

    @Test
    void createNormalisesEmailAndReturnsCurrencyOfCountry() {
        when(repository.existsByEmail("asha.rao@acme.com")).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponse created = service.create(request());

        ArgumentCaptor<Employee> saved = ArgumentCaptor.forClass(Employee.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("asha.rao@acme.com");
        assertThat(created.currency()).isEqualTo("INR");
        assertThat(created.annualSalary()).isEqualTo(1_800_000L);
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(repository.existsByEmail("asha.rao@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(DuplicateEmailException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void updateChangesAllFields() {
        Employee existing = employee("Old Name", "old@acme.com", "Analyst", "Finance", Country.US, 90_000);
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmailAndIdNot("asha.rao@acme.com", 7L)).thenReturn(false);

        EmployeeResponse updated = service.update(7L, request());

        assertThat(updated.fullName()).isEqualTo("Asha Rao");
        assertThat(updated.country()).isEqualTo(Country.IN);
        assertThat(updated.jobTitle()).isEqualTo("Software Engineer");
        assertThat(existing.getAnnualSalary()).isEqualTo(1_800_000L);
    }

    @Test
    void updateRejectsEmailOwnedByAnotherEmployee() {
        when(repository.findById(7L)).thenReturn(Optional.of(
                employee("Old Name", "old@acme.com", "Analyst", "Finance", Country.US, 90_000)));
        when(repository.existsByEmailAndIdNot("asha.rao@acme.com", 7L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(7L, request()))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void getUnknownEmployeeThrowsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void deleteUnknownEmployeeThrowsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(42L)).isInstanceOf(EmployeeNotFoundException.class);
        verify(repository, never()).delete(any(Employee.class));
    }
}
