package com.sesac.aibackend.service;

import com.sesac.aibackend.domain.Department;
import com.sesac.aibackend.domain.Employee;
import com.sesac.aibackend.repository.DepartmentRepository;
import com.sesac.aibackend.repository.EmployeeRepository;
import com.sesac.aibackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    public Optional<Department> findById(Long departmentId) {
        return departmentRepository.findById(departmentId);
    }

    @Transactional(readOnly = true)
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    @Transactional
    public void deleteById(Long departmentId) {
        departmentRepository.deleteById(departmentId);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long departmentId) {
        return departmentRepository.existsById(departmentId);
    }



}
