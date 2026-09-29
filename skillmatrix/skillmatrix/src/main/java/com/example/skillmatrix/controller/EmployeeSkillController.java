package com.example.skillmatrix.controller;

import java.util.List;

import com.example.skillmatrix.dto.request.EmployeeSkillRequest;
import com.example.skillmatrix.dto.request.ProficiencyRequest;
import com.example.skillmatrix.dto.response.EmployeeSkillResponse;
import com.example.skillmatrix.service.EmployeeSkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees/{employeeId}/skills")
public class EmployeeSkillController {

	private final EmployeeSkillService employeeSkillService;

	public EmployeeSkillController(EmployeeSkillService employeeSkillService) {
		this.employeeSkillService = employeeSkillService;
	}

	@PostMapping
	public ResponseEntity<EmployeeSkillResponse> add(@PathVariable Long employeeId,
			@Valid @RequestBody EmployeeSkillRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(employeeSkillService.add(employeeId, request));
	}

	@PutMapping("/{skillId}")
	public EmployeeSkillResponse update(@PathVariable Long employeeId, @PathVariable Long skillId,
			@Valid @RequestBody ProficiencyRequest request) {
		return employeeSkillService.update(employeeId, skillId, request);
	}

	@GetMapping
	public List<EmployeeSkillResponse> findByEmployee(@PathVariable Long employeeId) {
		return employeeSkillService.findByEmployee(employeeId);
	}

	@DeleteMapping("/{skillId}")
	public ResponseEntity<Void> delete(@PathVariable Long employeeId, @PathVariable Long skillId) {
		employeeSkillService.delete(employeeId, skillId);
		return ResponseEntity.noContent().build();
	}
}