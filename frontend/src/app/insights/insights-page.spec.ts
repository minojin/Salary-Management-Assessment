import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { InsightsApi } from '../core/insights-api';
import { CountryBreakdown, GroupSummary, Overview } from '../core/models';
import { InsightsPage, sortGroups } from './insights-page';
import { RangeBar } from './range-bar';

const group = (name: string, headcount: number, averageSalary: number): GroupSummary => ({
  name,
  stats: { headcount, minSalary: 0, maxSalary: 0, averageSalary },
});

describe('sortGroups', () => {
  const groups = [group('Recruiter', 5, 100), group('Engineer', 50, 300), group('Analyst', 10, 200)];

  it('sorts by a numeric stat', () => {
    expect(sortGroups(groups, { active: 'averageSalary', direction: 'desc' }).map((g) => g.name)).toEqual([
      'Engineer',
      'Analyst',
      'Recruiter',
    ]);
    expect(sortGroups(groups, { active: 'headcount', direction: 'asc' }).map((g) => g.name)).toEqual([
      'Recruiter',
      'Analyst',
      'Engineer',
    ]);
  });

  it('sorts by name and leaves the input untouched', () => {
    expect(sortGroups(groups, { active: 'name', direction: 'asc' }).map((g) => g.name)).toEqual([
      'Analyst',
      'Engineer',
      'Recruiter',
    ]);
    expect(groups[0].name).toBe('Recruiter');
  });

  it('keeps server order when sorting is cleared', () => {
    expect(sortGroups(groups, { active: 'name', direction: '' })).toBe(groups);
  });
});

describe('InsightsPage', () => {
  const overview: Overview = {
    totalEmployees: 3,
    departmentCount: 1,
    jobTitleCount: 1,
    countries: [
      { country: 'IN', countryName: 'India', currency: 'INR', stats: { headcount: 2, minSalary: 1, maxSalary: 2, averageSalary: 1 } },
      { country: 'US', countryName: 'United States', currency: 'USD', stats: { headcount: 1, minSalary: 1, maxSalary: 1, averageSalary: 1 } },
    ],
  };
  const breakdown = (code: 'IN' | 'US'): CountryBreakdown => ({
    summary: overview.countries.find((c) => c.country === code)!,
    byJobTitle: [group('Engineer', 1, 10)],
    byDepartment: [group('Engineering', 1, 10), group('People', 1, 5)],
  });

  let api: jasmine.SpyObj<InsightsApi>;

  beforeEach(() => {
    api = jasmine.createSpyObj<InsightsApi>('InsightsApi', ['overview', 'country']);
    api.overview.and.returnValue(of(overview));
    api.country.and.callFake((code) => of(breakdown(code as 'IN' | 'US')));
    TestBed.configureTestingModule({
      imports: [InsightsPage],
      providers: [provideRouter([]), { provide: InsightsApi, useValue: api }],
    });
  });

  it('opens the largest country first and switches grouping', () => {
    const fixture = TestBed.createComponent(InsightsPage);
    fixture.detectChanges();
    const page = fixture.componentInstance;

    expect(api.country).toHaveBeenCalledOnceWith('IN');
    expect(page.groups().map((g) => g.name)).toEqual(['Engineer']);

    page.groupBy.set('department');
    expect(page.groups().map((g) => g.name)).toEqual(['Engineering', 'People']);
  });

  it('does not refetch when the selected country is clicked again', () => {
    const fixture = TestBed.createComponent(InsightsPage);
    fixture.detectChanges();

    fixture.componentInstance.select('IN');
    fixture.componentInstance.select('US');

    expect(api.country.calls.allArgs()).toEqual([['IN'], ['US']]);
  });
});

describe('RangeBar', () => {
  it('positions min, average and max as percentages of the scale', () => {
    const fixture = TestBed.createComponent(RangeBar);
    fixture.componentRef.setInput('min', 150);
    fixture.componentRef.setInput('avg', 200);
    fixture.componentRef.setInput('max', 300);
    fixture.componentRef.setInput('scaleMin', 100);
    fixture.componentRef.setInput('scaleMax', 500);

    expect(fixture.componentInstance.pos()).toEqual({ min: 12.5, avg: 25, max: 50 });
  });

  it('does not divide by zero when the scale is a single value', () => {
    const fixture = TestBed.createComponent(RangeBar);
    for (const input of ['min', 'avg', 'max', 'scaleMin', 'scaleMax']) {
      fixture.componentRef.setInput(input, 100);
    }

    expect(fixture.componentInstance.pos()).toEqual({ min: 0, avg: 0, max: 0 });
  });
});
