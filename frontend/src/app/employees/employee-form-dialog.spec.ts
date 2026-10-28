import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';

import { EmployeeApi } from '../core/employee-api';
import { Employee, Meta } from '../core/models';
import { EmployeeFormDialog, EmployeeFormDialogData } from './employee-form-dialog';

const META: Meta = {
  countries: [
    { code: 'IN', name: 'India', currency: 'INR' },
    { code: 'US', name: 'United States', currency: 'USD' },
  ],
  departments: ['Engineering'],
  jobTitles: ['Software Engineer'],
};

const ASHA: Employee = {
  id: 1,
  fullName: 'Asha Rao',
  email: 'asha@acme.com',
  jobTitle: 'Software Engineer',
  department: 'Engineering',
  country: 'IN',
  currency: 'INR',
  annualSalary: 1800000,
  hireDate: '2021-04-01',
  createdAt: '2024-01-15T10:00:00Z',
  updatedAt: '2024-06-01T09:30:00Z',
};

describe('EmployeeFormDialog', () => {
  let api: jasmine.SpyObj<EmployeeApi>;
  let dialogRef: jasmine.SpyObj<MatDialogRef<EmployeeFormDialog, Employee>>;

  function create(data: EmployeeFormDialogData): EmployeeFormDialog {
    api = jasmine.createSpyObj<EmployeeApi>('EmployeeApi', ['create', 'update']);
    dialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    TestBed.configureTestingModule({
      imports: [EmployeeFormDialog],
      providers: [
        { provide: EmployeeApi, useValue: api },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });
    const fixture = TestBed.createComponent(EmployeeFormDialog);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  it('does not call the API while the form is invalid', () => {
    const dialog = create({ employee: null, meta: META });

    dialog.save();

    expect(api.create).not.toHaveBeenCalled();
    expect(dialog.form.controls.fullName.touched).toBeTrue();
  });

  it('rejects a hire date in the future', () => {
    const dialog = create({ employee: null, meta: META });

    dialog.form.controls.hireDate.setValue('2999-01-01');

    expect(dialog.form.controls.hireDate.hasError('future')).toBeTrue();
  });

  it('creates a new employee and closes with the saved record', () => {
    const dialog = create({ employee: null, meta: META });
    api.create.and.returnValue(of(ASHA));
    const { id: _id, currency: _currency, createdAt: _createdAt, updatedAt: _updatedAt, ...request } = ASHA;

    dialog.form.setValue(request);
    dialog.save();

    expect(api.create).toHaveBeenCalledWith(request);
    expect(dialogRef.close).toHaveBeenCalledWith(ASHA);
  });

  it('pre-fills the form when editing, shows the currency, and calls update', () => {
    const dialog = create({ employee: ASHA, meta: META });
    api.update.and.returnValue(of(ASHA));

    expect(dialog.form.controls.email.value).toBe('asha@acme.com');
    expect(dialog.currency()).toBe('INR');

    dialog.form.controls.annualSalary.setValue(2000000);
    dialog.save();

    expect(api.update).toHaveBeenCalledWith(1, jasmine.objectContaining({ annualSalary: 2000000 }));
  });

  it('shows server field errors next to the matching input', () => {
    const dialog = create({ employee: ASHA, meta: META });
    api.update.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { status: 409, errors: { email: 'is already used by another employee' } },
          }),
      ),
    );

    dialog.save();

    expect(dialog.form.controls.email.getError('server')).toBe('is already used by another employee');
    expect(dialog.saving()).toBeFalse();
    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('shows a general message when the error has no field details', () => {
    const dialog = create({ employee: ASHA, meta: META });
    api.update.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));

    dialog.save();

    expect(dialog.formError()).toContain('Something went wrong');
  });
});
