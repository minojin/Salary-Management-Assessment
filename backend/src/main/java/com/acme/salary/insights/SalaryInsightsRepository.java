package com.acme.salary.insights;

import com.acme.salary.employee.Country;
import com.acme.salary.employee.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Read-only aggregate queries. All grouping happens in SQL so only summary rows leave the database. */
public interface SalaryInsightsRepository extends Repository<Employee, Long> {

    @Query("""
            select new com.acme.salary.insights.GroupStats(
                e.country, count(e), min(e.annualSalary), max(e.annualSalary), avg(e.annualSalary))
            from Employee e
            group by e.country
            """)
    List<GroupStats<Country>> statsByCountry();

    @Query("""
            select new com.acme.salary.insights.GroupStats(
                e.jobTitle, count(e), min(e.annualSalary), max(e.annualSalary), avg(e.annualSalary))
            from Employee e
            where e.country = :country
            group by e.jobTitle
            order by e.jobTitle
            """)
    List<GroupStats<String>> statsByJobTitle(@Param("country") Country country);

    @Query("""
            select new com.acme.salary.insights.GroupStats(
                e.department, count(e), min(e.annualSalary), max(e.annualSalary), avg(e.annualSalary))
            from Employee e
            where e.country = :country
            group by e.department
            order by e.department
            """)
    List<GroupStats<String>> statsByDepartment(@Param("country") Country country);

    @Query("select count(distinct e.department) from Employee e")
    long countDistinctDepartments();

    @Query("select count(distinct e.jobTitle) from Employee e")
    long countDistinctJobTitles();
}
