package com.sesac.aibackend.controller;

import com.sesac.aibackend.domain.Department;
import com.sesac.aibackend.dto.DepartmentCreateRequest;
import com.sesac.aibackend.dto.DepartmentEmployeeResponse;
import com.sesac.aibackend.dto.DepartmentResponse;
import com.sesac.aibackend.error.NotFoundException;
import com.sesac.aibackend.service.DepartmentService;
import com.sesac.aibackend.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    @GetMapping()
    public List<DepartmentResponse> readAll() {
        return departmentService.findAll().stream().map(DepartmentResponse::from).toList();
    }

    @GetMapping("/{departmentId}")
    public DepartmentResponse read(@PathVariable Long departmentId) {
        Department department = departmentService.findById(departmentId)
                .orElseThrow(() -> NotFoundException.of("department ", departmentId));
        return DepartmentResponse.from(department);
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentCreateRequest req) {
        Department department = departmentService.save(req.toEntity());
        return ResponseEntity.created(URI.create("/departments/" + department.getDepartmentId()))
                .body(DepartmentResponse.from(department));
    }

    @DeleteMapping("/{departmentId}")
    public ResponseEntity<Void> delete(@PathVariable Long departmentId) {
        if (!departmentService.existsById(departmentId)) {
            throw NotFoundException.of("department ", departmentId);
        }
        departmentService.deleteById(departmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/with-employee/{name}")
    public List<DepartmentEmployeeResponse> findAllEmployeeByDepartmentName(@PathVariable String name) {
        return employeeService.findAllEmployeeByDepartmentName(name).stream().map(DepartmentEmployeeResponse::from).toList();
    }


}
