package com.example.skillmatrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import com.example.skillmatrix.repository.AllocationRepository;
import com.example.skillmatrix.repository.EmployeeRepository;
import com.example.skillmatrix.service.AllocationService;
import com.example.skillmatrix.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AllocationServiceTests {

	@Mock
	private AllocationRepository allocationRepository;

	@Mock
	private EmployeeRepository employeeRepository;

	@Mock
	private ProjectService projectService;

	@InjectMocks
	private AllocationService allocationService;

	@Test
	void rejectsAllocationOverRemainingCapacity() {
		Employee employee = new Employee("Alice");
		setId(employee, 1L);
		when(employeeRepository.findById(1L)).thenReturn(java.util.Optional.of(employee));
		when(projectService.getProject(2L)).thenReturn(new Project("Project"));
		Allocation existing = new Allocation(employee, new Project("Existing"), 70, AllocationType.PARTIAL);
		setId(existing, 4L);
		when(allocationRepository.findByEmployeeId(1L)).thenReturn(List.of(existing));

		CapacityExceededException exception = assertThrows(CapacityExceededException.class,
				() -> allocationService.create(2L, new AllocationRequest(1L, 40, AllocationType.PARTIAL)));
		assertEquals("Employee 1 has only 30% capacity available, but 40% was requested.", exception.getMessage());
	}

	@Test
	void fullAllocationMustBeOneHundredPercent() {
		assertThrows(InvalidRequestException.class,
				() -> allocationService.update(1L, new AllocationUpdateRequest(80, AllocationType.FULL)));
	}

	@Test
	void updateDoesNotCountTheCurrentAllocationTwice() {
		Employee employee = new Employee("Alice");
		setId(employee, 1L);
		Project project = new Project("Project");
		Allocation current = new Allocation(employee, project, 60, AllocationType.PARTIAL);
		Allocation other = new Allocation(employee, new Project("Other"), 20, AllocationType.PARTIAL);
		setId(current, 10L);
		setId(other, 11L);
		when(allocationRepository.findById(10L)).thenReturn(java.util.Optional.of(current));
		when(allocationRepository.findByEmployeeId(1L)).thenReturn(List.of(current, other));
		when(allocationRepository.save(any(Allocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AllocationResponse response = allocationService.update(10L,
				new AllocationUpdateRequest(80, AllocationType.PARTIAL));

		assertEquals(80, response.allocationPercentage());
	}

	private void setId(Object entity, Long id) {
		try {
			var field = entity.getClass().getDeclaredField("id");
			field.setAccessible(true);
			field.set(entity, id);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}
}