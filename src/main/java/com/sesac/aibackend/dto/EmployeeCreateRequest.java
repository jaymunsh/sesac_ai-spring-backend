package com.sesac.aibackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record EmployeeCreateRequest(
        @NotBlank String name,
        @Min(0) Long departmentId
) {
}
