package com.acme.salary.seed;

import com.acme.salary.employee.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeedRunnerTest {

    @Mock
    EmployeeRepository repository;

    @Test
    void doesNothingWhenDisabled() {
        new SeedRunner(repository, new SeedProperties(0, false, 42)).run(null);

        verifyNoInteractions(repository);
    }

    @Test
    void skipsWhenEmployeesAlreadyExist() {
        when(repository.count()).thenReturn(5L);

        new SeedRunner(repository, new SeedProperties(10, false, 42)).run(null);

        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void seedsEmptyTableInBatchesOfOneThousand() {
        when(repository.count()).thenReturn(0L);

        new SeedRunner(repository, new SeedProperties(2_500, false, 42)).run(null);

        verify(repository, times(3)).saveAll(anyList());
    }

    @Test
    void resetDeletesExistingDataBeforeSeeding() {
        new SeedRunner(repository, new SeedProperties(10, true, 42)).run(null);

        verify(repository).deleteAllInBatch();
        verify(repository, never()).count();
        verify(repository).saveAll(anyList());
    }
}
