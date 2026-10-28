import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DatePipe } from '@angular/common';
import { BehaviorSubject, catchError, debounceTime, distinctUntilChanged, filter, of, switchMap, tap } from 'rxjs';

import { EmployeeApi } from '../core/employee-api';
import { CountryCode, Employee, EmployeeQuery, Meta, Page } from '../core/models';
import { ConfirmDialog, ConfirmDialogData } from '../shared/confirm-dialog';
import { MoneyPipe } from '../shared/money.pipe';
import { EmployeeFormDialog, EmployeeFormDialogData } from './employee-form-dialog';

const EMPTY_META: Meta = { countries: [], departments: [], jobTitles: [] };

export const INITIAL_QUERY: EmployeeQuery = {
  page: 0,
  size: 20,
  sort: 'fullName',
  direction: 'asc',
};

@Component({
  selector: 'app-employees-page',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MoneyPipe,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressBarModule,
  ],
  templateUrl: './employees-page.html',
  styleUrl: './employees-page.scss',
})
export class EmployeesPage implements OnInit {
  private readonly api = inject(EmployeeApi);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  readonly columns = ['fullName', 'jobTitle', 'department', 'country', 'annualSalary', 'hireDate', 'actions'];

  readonly filters = new FormGroup({
    search: new FormControl('', { nonNullable: true }),
    country: new FormControl<CountryCode | null>(null),
    department: new FormControl<string | null>(null),
    jobTitle: new FormControl<string | null>(null),
  });

  /** Single source of truth for what is shown; every UI change produces a new query. */
  readonly query$ = new BehaviorSubject<EmployeeQuery>(INITIAL_QUERY);

  readonly page = signal<Page<Employee> | null>(null);
  readonly meta = signal<Meta>(EMPTY_META);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadMeta();

    this.filters.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
        takeUntilDestroyed(this.destroyRef),
      )
      // A new filter means a new result set, so go back to the first page.
      .subscribe((filters) => this.query$.next({ ...this.query$.value, ...filters, page: 0 }));

    this.query$
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set(null);
        }),
        switchMap((query) =>
          this.api.search(query).pipe(
            catchError(() => {
              this.error.set('Could not load employees. Please try again.');
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => {
        this.loading.set(false);
        if (page) {
          this.page.set(page);
        }
      });
  }

  onSort(sort: Sort): void {
    const active = sort.direction ? sort.active : INITIAL_QUERY.sort;
    const direction = sort.direction || INITIAL_QUERY.direction;
    this.query$.next({ ...this.query$.value, sort: active, direction, page: 0 });
  }

  onPage(event: PageEvent): void {
    this.query$.next({ ...this.query$.value, page: event.pageIndex, size: event.pageSize });
  }

  clearFilters(): void {
    this.filters.reset({ search: '', country: null, department: null, jobTitle: null });
  }

  hasFilters(): boolean {
    const { search, country, department, jobTitle } = this.filters.value;
    return !!(search || country || department || jobTitle);
  }

  countryName(code: CountryCode): string {
    return this.meta().countries.find((c) => c.code === code)?.name ?? code;
  }

  openForm(employee: Employee | null = null): void {
    this.dialog
      .open<EmployeeFormDialog, EmployeeFormDialogData, Employee>(EmployeeFormDialog, {
        data: { employee, meta: this.meta() },
        width: '640px',
        maxWidth: '95vw',
        autoFocus: 'first-tabbable',
      })
      .afterClosed()
      .pipe(filter((saved): saved is Employee => !!saved))
      .subscribe((saved) => {
        this.snackBar.open(`${saved.fullName} ${employee ? 'updated' : 'added'}`, 'OK', { duration: 3000 });
        this.refresh();
      });
  }

  confirmDelete(employee: Employee): void {
    this.dialog
      .open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
        data: {
          title: 'Delete employee?',
          message: `${employee.fullName} (${employee.email}) will be permanently removed.`,
          confirmLabel: 'Delete',
        },
      })
      .afterClosed()
      .pipe(
        filter(Boolean),
        switchMap(() => this.api.delete(employee.id)),
      )
      .subscribe({
        next: () => {
          this.snackBar.open(`${employee.fullName} deleted`, 'OK', { duration: 3000 });
          this.refresh();
        },
        error: () => this.snackBar.open('Delete failed. Please try again.', 'OK', { duration: 5000 }),
      });
  }

  /** Re-runs the current query and refreshes dropdown options (a save can add a new department/title). */
  private refresh(): void {
    this.query$.next({ ...this.query$.value });
    this.loadMeta();
  }

  private loadMeta(): void {
    this.api.meta().subscribe({ next: (meta) => this.meta.set(meta), error: () => {} });
  }
}
