package com.sesac.aibackend.dto;

import com.sesac.aibackend.domain.Department;
import com.sesac.aibackend.domain.Employee;

import java.util.List;

public record DepartmentEmployeeResponse(
        Long employeeId,
        String employeeName,
        String departmentName
) {
    public static DepartmentEmployeeResponse from(Employee employee) {
        return new DepartmentEmployeeResponse(
                employee.getEmployeeId(),
                employee.getName(),
                employee.getDepartment().getName()
        );
    }
}
