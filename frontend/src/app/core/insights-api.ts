import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { CountryBreakdown, CountryCode, Overview } from './models';

@Injectable({ providedIn: 'root' })
export class InsightsApi {
  private readonly http = inject(HttpClient);

  overview(): Observable<Overview> {
    return this.http.get<Overview>('/api/insights/overview');
  }

  country(code: CountryCode): Observable<CountryBreakdown> {
    return this.http.get<CountryBreakdown>(`/api/insights/countries/${code}`);
  }
}
