package com.example.skillmatrix.controller;

import java.util.List;

import com.example.skillmatrix.dto.request.AllocationRequest;
import com.example.skillmatrix.dto.request.AllocationUpdateRequest;
import com.example.skillmatrix.dto.response.AllocationResponse;
import com.example.skillmatrix.service.AllocationService;
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
public class AllocationController {

	private final AllocationService allocationService;

	public AllocationController(AllocationService allocationService) {
		this.allocationService = allocationService;
	}

	@PostMapping("/projects/{projectId}/allocations")
	public ResponseEntity<AllocationResponse> create(@PathVariable Long projectId,
			@Valid @RequestBody AllocationRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(allocationService.create(projectId, request));
	}

	@PutMapping("/allocations/{allocationId}")
	public AllocationResponse update(@PathVariable Long allocationId,
			@Valid @RequestBody AllocationUpdateRequest request) {
		return allocationService.update(allocationId, request);
	}

	@DeleteMapping("/allocations/{allocationId}")
	public ResponseEntity<Void> delete(@PathVariable Long allocationId) {
		allocationService.delete(allocationId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/employees/{employeeId}/allocations")
	public List<AllocationResponse> findByEmployee(@PathVariable Long employeeId) {
		return allocationService.findByEmployee(employeeId);
	}

	@GetMapping("/projects/{projectId}/allocations")
	public List<AllocationResponse> findByProject(@PathVariable Long projectId) {
		return allocationService.findByProject(projectId);
	}
}