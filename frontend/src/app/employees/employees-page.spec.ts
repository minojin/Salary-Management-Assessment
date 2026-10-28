import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of } from 'rxjs';

import { EmployeeApi } from '../core/employee-api';
import { EmployeeQuery } from '../core/models';
import { EmployeesPage, INITIAL_QUERY } from './employees-page';

describe('EmployeesPage', () => {
  let api: jasmine.SpyObj<EmployeeApi>;

  function create(): EmployeesPage {
    api = jasmine.createSpyObj<EmployeeApi>('EmployeeApi', ['search', 'meta', 'delete']);
    api.search.and.returnValue(of({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }));
    api.meta.and.returnValue(of({ countries: [], departments: [], jobTitles: [] }));
    TestBed.configureTestingModule({
      imports: [EmployeesPage],
      providers: [{ provide: EmployeeApi, useValue: api }],
    });
    const fixture = TestBed.createComponent(EmployeesPage);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  const lastQuery = (): EmployeeQuery => api.search.calls.mostRecent().args[0];

  it('loads the first page sorted by name on start', () => {
    create();

    expect(api.search).toHaveBeenCalledOnceWith(INITIAL_QUERY);
  });

  it('debounces filter changes and goes back to the first page', fakeAsync(() => {
    const page = create();
    page.onPage({ pageIndex: 3, pageSize: 20, length: 1000 });
    expect(lastQuery().page).toBe(3);

    page.filters.controls.search.setValue('ra');
    page.filters.controls.search.setValue('rao');
    tick(300);

    expect(lastQuery()).toEqual(jasmine.objectContaining({ search: 'rao', page: 0 }));
    // initial load + page change + one debounced search, not one call per keystroke
    expect(api.search).toHaveBeenCalledTimes(3);
  }));

  it('resets to the default sort when sorting is cleared', () => {
    const page = create();

    page.onSort({ active: 'annualSalary', direction: 'desc' });
    expect(lastQuery()).toEqual(jasmine.objectContaining({ sort: 'annualSalary', direction: 'desc', page: 0 }));

    page.onSort({ active: 'annualSalary', direction: '' });
    expect(lastQuery()).toEqual(jasmine.objectContaining({ sort: 'fullName', direction: 'asc' }));
  });
});
