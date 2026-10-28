import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    title: 'Employees · ACME Salaries',
    loadComponent: () => import('./employees/employees-page').then((m) => m.EmployeesPage),
  },
  {
    path: 'insights',
    title: 'Salary insights · ACME Salaries',
    loadComponent: () => import('./insights/insights-page').then((m) => m.InsightsPage),
  },
  { path: '**', redirectTo: 'employees' },
];
