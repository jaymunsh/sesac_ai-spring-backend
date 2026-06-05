package com.sesac.aibackend.service;

import com.sesac.aibackend.domain.Employee;
import com.sesac.aibackend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> findById(Long employeeId) {
        return employeeRepository.findById(employeeId);
    }

    @Transactional
    public void deleteById(Long employeeId) {
        employeeRepository.deleteById(employeeId);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long employeeId) {
        return employeeRepository.existsById(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Employee> findAllEmployeeByDepartmentName(String name) {
        return employeeRepository.findAllEmployeeByDepartmentName(name);
    }


}
