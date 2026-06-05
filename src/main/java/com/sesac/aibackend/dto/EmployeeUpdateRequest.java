package com.sesac.aibackend.dto;

import com.sesac.aibackend.domain.Employee;
import jakarta.validation.constraints.NotBlank;

public record EmployeeUpdateRequest(
        @NotBlank Long employeeId,
        @NotBlank String name
) {
    public Employee toEntity() {
        return Employee.builder().employeeId(employeeId).name(name).build();
    }
}
