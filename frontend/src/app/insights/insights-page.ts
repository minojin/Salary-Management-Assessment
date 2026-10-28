import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';

import { InsightsApi } from '../core/insights-api';
import { CountryBreakdown, CountryCode, GroupSummary, Overview, SalaryStats } from '../core/models';
import { MoneyPipe } from '../shared/money.pipe';
import { RangeBar } from './range-bar';

type GroupBy = 'jobTitle' | 'department';
type SortKey = 'name' | keyof SalaryStats;

/** Sorts groups for display. Pure, so it is easy to test. */
export function sortGroups(groups: GroupSummary[], sort: Sort): GroupSummary[] {
  if (!sort.direction) {
    return groups;
  }
  const key = sort.active as SortKey;
  const sign = sort.direction === 'asc' ? 1 : -1;
  return [...groups].sort((a, b) => {
    const diff = key === 'name' ? a.name.localeCompare(b.name) : a.stats[key] - b.stats[key];
    return diff * sign;
  });
}

@Component({
  selector: 'app-insights-page',
  imports: [
    MoneyPipe,
    RangeBar,
    RouterLink,
    MatTableModule,
    MatSortModule,
    MatButtonToggleModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './insights-page.html',
  styleUrl: './insights-page.scss',
})
export class InsightsPage implements OnInit {
  private readonly api = inject(InsightsApi);

  readonly countryColumns = ['country', 'headcount', 'minSalary', 'averageSalary', 'maxSalary'];
  readonly groupColumns = ['name', 'headcount', 'minSalary', 'averageSalary', 'maxSalary', 'range'];

  readonly overview = signal<Overview | null>(null);
  readonly breakdown = signal<CountryBreakdown | null>(null);
  readonly selected = signal<CountryCode | null>(null);
  readonly groupBy = signal<GroupBy>('jobTitle');
  readonly sort = signal<Sort>({ active: 'averageSalary', direction: 'desc' });
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly groups = computed(() => {
    const breakdown = this.breakdown();
    if (!breakdown) {
      return [];
    }
    const groups = this.groupBy() === 'jobTitle' ? breakdown.byJobTitle : breakdown.byDepartment;
    return sortGroups(groups, this.sort());
  });

  ngOnInit(): void {
    this.api.overview().subscribe({
      next: (overview) => {
        this.overview.set(overview);
        // Start with the largest country so the page is useful immediately.
        const first = overview.countries[0];
        if (first) {
          this.select(first.country);
        }
      },
      error: () => this.error.set('Could not load salary insights. Please try again.'),
    });
  }

  select(country: CountryCode): void {
    if (country === this.selected()) {
      return;
    }
    this.selected.set(country);
    this.loading.set(true);
    this.api.country(country).subscribe({
      next: (breakdown) => {
        this.breakdown.set(breakdown);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Could not load the country breakdown. Please try again.');
      },
    });
  }
}
