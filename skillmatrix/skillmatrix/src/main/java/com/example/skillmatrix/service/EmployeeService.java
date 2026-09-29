package com.example.skillmatrix.service;

import java.util.List;

import com.example.skillmatrix.dto.request.EmployeeRequest;
import com.example.skillmatrix.dto.response.EmployeeResponse;
import com.example.skillmatrix.entity.Employee;
import com.example.skillmatrix.exception.ResourceNotFoundException;
import com.example.skillmatrix.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

	private final EmployeeRepository employeeRepository;

	public EmployeeService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	@Transactional
	public EmployeeResponse create(EmployeeRequest request) {
		return toResponse(employeeRepository.save(new Employee(request.name())));
	}

	@Transactional(readOnly = true)
	public List<EmployeeResponse> findAll() {
		return employeeRepository.findAll().stream().map(EmployeeService::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public EmployeeResponse findById(Long id) {
		return toResponse(getEmployee(id));
	}

	@Transactional
	public EmployeeResponse update(Long id, EmployeeRequest request) {
		Employee employee = getEmployee(id);
		employee.setName(request.name());
		return toResponse(employeeRepository.save(employee));
	}

	@Transactional
	public void delete(Long id) {
		employeeRepository.delete(getEmployee(id));
	}

	private Employee getEmployee(Long id) {
		return employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Employee with id " + id + " not found"));
	}

	private static EmployeeResponse toResponse(Employee employee) {
		return new EmployeeResponse(employee.getId(), employee.getName());
	}
}