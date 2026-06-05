package com.sesac.aibackend.dto;

import com.sesac.aibackend.domain.Department;
import jakarta.validation.constraints.NotNull;

public record DepartmentCreateRequest(
        @NotNull String name
) {
    public Department toEntity() {
        return Department.builder().name(name).build();
    }
}
