-- Support sorting the employee list by when records were added or last changed.
CREATE INDEX ix_employee_created_at ON employee (created_at);
CREATE INDEX ix_employee_updated_at ON employee (updated_at);
