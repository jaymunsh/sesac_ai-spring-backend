package com.sesac.aibackend.controller;

import com.sesac.aibackend.domain.Department;
import com.sesac.aibackend.domain.Employee;
import com.sesac.aibackend.dto.EmployeeCreateRequest;
import com.sesac.aibackend.dto.EmployeeResponse;
import com.sesac.aibackend.dto.EmployeeUpdateRequest;
import com.sesac.aibackend.error.NotFoundException;
import com.sesac.aibackend.service.DepartmentService;
import com.sesac.aibackend.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeCreateRequest req) {
        Department department = departmentService.findById(req.departmentId())
                .orElseThrow(() -> NotFoundException.of("department ", req.departmentId()));

        Employee employee = Employee.builder()
                .name(req.name())
                .department(department)
                .build();

        employeeService.save(employee);

        return ResponseEntity.created(URI.create("/employess/" + employee.getEmployeeId()))
                .body(EmployeeResponse.from(employee));
    }

    @GetMapping("/{id}")
    public EmployeeResponse read(@PathVariable Long employeeId) {
        Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> NotFoundException.of("employee ", employeeId));
        return EmployeeResponse.from(employee);
    }

    @PutMapping
    public ResponseEntity<EmployeeResponse> update(@Valid @RequestBody EmployeeUpdateRequest req) {
        if (!employeeService.existsById(req.toEntity().getEmployeeId())) {
            throw NotFoundException.of("employee", req.toEntity().getEmployeeId());
        }
        Employee employee = employeeService.save(req.toEntity());
        return ResponseEntity.created(URI.create("/employess/" + employee.getEmployeeId()))
                .body(EmployeeResponse.from(employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long employeeId) {
        if (!employeeService.existsById(employeeId)) {
            throw NotFoundException.of("employee ", employeeId);
        }
        employeeService.deleteById(employeeId);
        return ResponseEntity.noContent().build();
    }

}
