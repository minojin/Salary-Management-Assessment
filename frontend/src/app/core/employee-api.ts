import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Employee, EmployeeQuery, EmployeeRequest, Meta, Page } from './models';

@Injectable({ providedIn: 'root' })
export class EmployeeApi {
  private readonly http = inject(HttpClient);

  search(query: EmployeeQuery): Observable<Page<Employee>> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sort', `${query.sort},${query.direction}`);
    // Only send filters that are set, so the URL stays readable and the server sees "no filter".
    for (const key of ['search', 'country', 'department', 'jobTitle'] as const) {
      const value = query[key]?.trim();
      if (value) {
        params = params.set(key, value);
      }
    }
    return this.http.get<Page<Employee>>('/api/employees', { params });
  }

  create(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>('/api/employees', request);
  }

  update(id: number, request: EmployeeRequest): Observable<Employee> {
    return this.http.put<Employee>(`/api/employees/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/employees/${id}`);
  }

  meta(): Observable<Meta> {
    return this.http.get<Meta>('/api/meta');
  }
}
