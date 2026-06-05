package com.sesac.aibackend.dto;

public record DepartmentResponse(Long departmentId, String name) {
    public static DepartmentResponse from(com.sesac.aibackend.domain.Department department) {
        return new DepartmentResponse(department.getDepartmentId(), department.getName());
    }
}
