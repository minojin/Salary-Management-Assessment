CREATE TABLE employee (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name      VARCHAR(120) NOT NULL,
    email          VARCHAR(160) NOT NULL,
    job_title      VARCHAR(80)  NOT NULL,
    department     VARCHAR(80)  NOT NULL,
    country_code   CHAR(2)      NOT NULL,
    annual_salary  INTEGER      NOT NULL CHECK (annual_salary > 0),
    hire_date      DATE         NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX ux_employee_email ON employee (email);
CREATE INDEX ix_employee_country ON employee (country_code);
CREATE INDEX ix_employee_country_job_title ON employee (country_code, job_title);
CREATE INDEX ix_employee_country_department ON employee (country_code, department);
CREATE INDEX ix_employee_full_name ON employee (full_name);
