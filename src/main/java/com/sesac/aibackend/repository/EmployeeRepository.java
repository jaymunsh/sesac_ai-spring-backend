package com.sesac.aibackend.repository;

import com.sesac.aibackend.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("""
            select e from Employee e
            join fetch e.department
            where e.department.name = :name
            order by e.employeeId
            """)
    List<Employee> findAllEmployeeByDepartmentName(@Param("name") String name);

}
