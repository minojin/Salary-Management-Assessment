/** Types mirror the Spring Boot API contract (see backend `*Response` records). */

export type CountryCode = 'IN' | 'US' | 'GB' | 'DE' | 'FR' | 'CA' | 'AU' | 'SG';

export interface CountryOption {
  code: CountryCode;
  name: string;
  currency: string;
}

export interface Meta {
  countries: CountryOption[];
  departments: string[];
  jobTitles: string[];
}

export interface Employee {
  id: number;
  fullName: string;
  email: string;
  jobTitle: string;
  department: string;
  country: CountryCode;
  currency: string;
  annualSalary: number;
  hireDate: string; // ISO yyyy-MM-dd
  createdAt: string; // ISO instant
  updatedAt: string; // ISO instant
}

export type EmployeeRequest = Omit<Employee, 'id' | 'currency' | 'createdAt' | 'updatedAt'>;

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface EmployeeQuery {
  search?: string;
  country?: CountryCode | null;
  department?: string | null;
  jobTitle?: string | null;
  page: number;
  size: number;
  sort: string;
  direction: 'asc' | 'desc';
}

export interface SalaryStats {
  headcount: number;
  minSalary: number;
  maxSalary: number;
  averageSalary: number;
}

export interface CountrySummary {
  country: CountryCode;
  countryName: string;
  currency: string;
  stats: SalaryStats;
}

export interface GroupSummary {
  name: string;
  stats: SalaryStats;
}

export interface Overview {
  totalEmployees: number;
  departmentCount: number;
  jobTitleCount: number;
  countries: CountrySummary[];
}

export interface CountryBreakdown {
  summary: CountrySummary;
  byJobTitle: GroupSummary[];
  byDepartment: GroupSummary[];
}

/** RFC 7807 problem body returned by the API on errors. */
export interface ApiProblem {
  status: number;
  detail?: string;
  errors?: Record<string, string>;
}
