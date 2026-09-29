package com.example.skillmatrix.service;

import java.util.List;

import com.example.skillmatrix.dto.request.AllocationRequest;
import com.example.skillmatrix.dto.request.AllocationUpdateRequest;
import com.example.skillmatrix.dto.response.AllocationResponse;
import com.example.skillmatrix.entity.Allocation;
import com.example.skillmatrix.entity.AllocationType;
import com.example.skillmatrix.entity.Employee;
import com.example.skillmatrix.entity.Project;
import com.example.skillmatrix.exception.CapacityExceededException;
import com.example.skillmatrix.exception.InvalidRequestException;
import com.example.skillmatrix.exception.ResourceNotFoundException;
import com.example.skillmatrix.repository.AllocationRepository;
import com.example.skillmatrix.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AllocationService {

	private final AllocationRepository allocationRepository;
	private final EmployeeRepository employeeRepository;
	private final ProjectService projectService;

	public AllocationService(AllocationRepository allocationRepository, EmployeeRepository employeeRepository,
			ProjectService projectService) {
		this.allocationRepository = allocationRepository;
		this.employeeRepository = employeeRepository;
		this.projectService = projectService;
	}

	@Transactional
	public AllocationResponse create(Long projectId, AllocationRequest request) {
		validateAllocation(request.allocationPercentage(), request.allocationType());
		Employee employee = getEmployee(request.employeeId());
		Project project = projectService.getProject(projectId);
		ensureCapacity(employee.getId(), request.allocationPercentage(), null);
		return toResponse(allocationRepository.save(new Allocation(employee, project,
				request.allocationPercentage(), request.allocationType())));
	}

	@Transactional
	public AllocationResponse update(Long allocationId, AllocationUpdateRequest request) {
		validateAllocation(request.allocationPercentage(), request.allocationType());
		Allocation allocation = allocationRepository.findById(allocationId)
				.orElseThrow(() -> new ResourceNotFoundException("Allocation with id " + allocationId + " not found"));
		ensureCapacity(allocation.getEmployee().getId(), request.allocationPercentage(), allocationId);
		allocation.setAllocationPercentage(request.allocationPercentage());
		allocation.setAllocationType(request.allocationType());
		return toResponse(allocationRepository.save(allocation));
	}

	@Transactional
	public void delete(Long allocationId) {
		Allocation allocation = allocationRepository.findById(allocationId)
				.orElseThrow(() -> new ResourceNotFoundException("Allocation with id " + allocationId + " not found"));
		allocationRepository.delete(allocation);
	}

	@Transactional(readOnly = true)
	public List<AllocationResponse> findByEmployee(Long employeeId) {
		getEmployee(employeeId);
		return allocationRepository.findByEmployeeId(employeeId).stream().map(AllocationService::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<AllocationResponse> findByProject(Long projectId) {
		projectService.getProject(projectId);
		return allocationRepository.findByProjectId(projectId).stream().map(AllocationService::toResponse).toList();
	}

	private void ensureCapacity(Long employeeId, int requested, Long excludedAllocationId) {
		int currentAllocation = allocationRepository.findByEmployeeId(employeeId).stream()
				.filter(allocation -> !allocation.getId().equals(excludedAllocationId))
				.mapToInt(Allocation::getAllocationPercentage).sum();
		int availableCapacity = 100 - currentAllocation;
		if (requested > availableCapacity) {
			throw new CapacityExceededException("Employee " + employeeId + " has only " + availableCapacity
					+ "% capacity available, but " + requested + "% was requested.");
		}
	}

	private void validateAllocation(Integer percentage, AllocationType type) {
		if (percentage == null || percentage < 1 || percentage > 100 || type == null) {
			throw new InvalidRequestException("allocationPercentage must be between 1 and 100 and allocationType is required");
		}
		if (type == AllocationType.FULL && percentage != 100) {
			throw new InvalidRequestException("FULL allocation must be exactly 100%");
		}
		if (type == AllocationType.PARTIAL && percentage >= 100) {
			throw new InvalidRequestException("PARTIAL allocation must be greater than 0 and less than 100%");
		}
	}

	private Employee getEmployee(Long id) {
		return employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Employee with id " + id + " not found"));
	}

	private static AllocationResponse toResponse(Allocation allocation) {
		return new AllocationResponse(allocation.getId(), allocation.getEmployee().getId(),
				allocation.getEmployee().getName(), allocation.getProject().getId(), allocation.getProject().getName(),
				allocation.getAllocationPercentage(), allocation.getAllocationType());
	}
}