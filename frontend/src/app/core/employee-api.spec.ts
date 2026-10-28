import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { EmployeeApi } from './employee-api';

describe('EmployeeApi', () => {
  let api: EmployeeApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(EmployeeApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends paging and sort, and omits empty filters', () => {
    api.search({ search: '  ', country: null, page: 2, size: 50, sort: 'annualSalary', direction: 'desc' }).subscribe();

    const req = http.expectOne((r) => r.url === '/api/employees');
    expect(req.request.params.keys().sort()).toEqual(['page', 'size', 'sort']);
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('50');
    expect(req.request.params.get('sort')).toBe('annualSalary,desc');
    req.flush({ content: [], page: 2, size: 50, totalElements: 0, totalPages: 0 });
  });

  it('sends trimmed filters when set', () => {
    api
      .search({
        search: ' rao ',
        country: 'IN',
        department: 'Engineering',
        jobTitle: 'Software Engineer',
        page: 0,
        size: 20,
        sort: 'fullName',
        direction: 'asc',
      })
      .subscribe();

    const req = http.expectOne((r) => r.url === '/api/employees');
    expect(req.request.params.get('search')).toBe('rao');
    expect(req.request.params.get('country')).toBe('IN');
    expect(req.request.params.get('department')).toBe('Engineering');
    expect(req.request.params.get('jobTitle')).toBe('Software Engineer');
    req.flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
  });

  it('uses the right verbs for create, update and delete', () => {
    const body = {
      fullName: 'A',
      email: 'a@acme.com',
      jobTitle: 'SE',
      department: 'Eng',
      country: 'IN' as const,
      annualSalary: 1,
      hireDate: '2020-01-01',
    };
    api.create(body).subscribe();
    api.update(7, body).subscribe();
    api.delete(7).subscribe();

    expect(http.expectOne({ method: 'POST', url: '/api/employees' }).request.body).toEqual(body);
    http.expectOne({ method: 'PUT', url: '/api/employees/7' }).flush({});
    http.expectOne({ method: 'DELETE', url: '/api/employees/7' }).flush(null);
  });
});
