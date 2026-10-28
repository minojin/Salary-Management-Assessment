import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Observable } from 'rxjs';

import { EmployeeApi } from '../core/employee-api';
import { ApiProblem, CountryCode, Employee, EmployeeRequest, Meta } from '../core/models';

export interface EmployeeFormDialogData {
  employee: Employee | null;
  meta: Meta;
}

export function todayIso(): string {
  const now = new Date();
  const offset = now.getTimezoneOffset() * 60_000;
  return new Date(now.getTime() - offset).toISOString().slice(0, 10);
}

/** Mirrors the server's @PastOrPresent rule so the user gets instant feedback. */
function notInFuture(control: AbstractControl<string | null>): ValidationErrors | null {
  return control.value && control.value > todayIso() ? { future: true } : null;
}

@Component({
  selector: 'app-employee-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatButtonModule,
  ],
  templateUrl: './employee-form-dialog.html',
  styleUrl: './employee-form-dialog.scss',
})
export class EmployeeFormDialog {
  private readonly api = inject(EmployeeApi);
  private readonly dialogRef = inject<MatDialogRef<EmployeeFormDialog, Employee>>(MatDialogRef);
  readonly data = inject<EmployeeFormDialogData>(MAT_DIALOG_DATA);

  readonly isEdit = this.data.employee !== null;
  readonly today = todayIso();
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);

  readonly form = new FormGroup({
    fullName: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(120)] }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email, Validators.maxLength(160)],
    }),
    jobTitle: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(80)] }),
    department: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(80)] }),
    country: new FormControl<CountryCode | null>(null, Validators.required),
    annualSalary: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    hireDate: new FormControl<string | null>(null, [Validators.required, notInFuture]),
  });

  private readonly country = toSignal(this.form.controls.country.valueChanges, {
    initialValue: this.data.employee?.country ?? null,
  });
  readonly currency = computed(
    () => this.data.meta.countries.find((c) => c.code === this.country())?.currency ?? '',
  );

  private readonly jobTitleInput = toSignal(this.form.controls.jobTitle.valueChanges, { initialValue: '' });
  private readonly departmentInput = toSignal(this.form.controls.department.valueChanges, { initialValue: '' });
  readonly jobTitleOptions = computed(() => matching(this.data.meta.jobTitles, this.jobTitleInput()));
  readonly departmentOptions = computed(() => matching(this.data.meta.departments, this.departmentInput()));

  constructor() {
    const employee = this.data.employee;
    if (employee) {
      const { id: _id, currency: _currency, createdAt: _createdAt, updatedAt: _updatedAt, ...editable } = employee;
      this.form.setValue(editable);
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.formError.set(null);
    const request = this.form.getRawValue() as EmployeeRequest;
    const call: Observable<Employee> = this.data.employee
      ? this.api.update(this.data.employee.id, request)
      : this.api.create(request);

    call.subscribe({
      next: (saved) => this.dialogRef.close(saved),
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.applyServerErrors(error);
      },
    });
  }

  /** Shows server-side field errors (e.g. duplicate email) next to the matching input. */
  private applyServerErrors(error: HttpErrorResponse): void {
    const problem = error.error as ApiProblem | null;
    const fieldErrors = problem?.errors ?? {};
    let shown = false;
    for (const [field, message] of Object.entries(fieldErrors)) {
      const control = this.form.get(field);
      if (control) {
        control.setErrors({ server: message });
        control.markAsTouched();
        shown = true;
      }
    }
    if (!shown) {
      this.formError.set(problem?.detail ?? 'Something went wrong while saving. Please try again.');
    }
  }
}

function matching(options: string[], input: string | null): string[] {
  const term = (input ?? '').trim().toLowerCase();
  return term ? options.filter((o) => o.toLowerCase().includes(term)) : options;
}
