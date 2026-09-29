package com.example.skillmatrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.skillmatrix.dto.request.EmployeeRequest;
import com.example.skillmatrix.dto.request.SkillRequest;
import com.example.skillmatrix.entity.Employee;
import com.example.skillmatrix.entity.Skill;
import com.example.skillmatrix.exception.DuplicateResourceException;
import com.example.skillmatrix.exception.ResourceNotFoundException;
import com.example.skillmatrix.repository.EmployeeRepository;
import com.example.skillmatrix.repository.SkillRepository;
import com.example.skillmatrix.service.EmployeeService;
import com.example.skillmatrix.service.SkillService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeAndSkillServiceTests {

	@Mock
	private EmployeeRepository employeeRepository;

	@Mock
	private SkillRepository skillRepository;

	@InjectMocks
	private EmployeeService employeeService;

	@InjectMocks
	private SkillService skillService;

	@Test
	void createsEmployee() {
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertEquals("Alice", employeeService.create(new EmployeeRequest("Alice")).name());
	}

	@Test
	void reportsMissingEmployee() {
		when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> employeeService.findById(42L));

		assertEquals("Employee with id 42 not found", exception.getMessage());
	}

	@Test
	void rejectsDuplicateSkillName() {
		when(skillRepository.existsByName("Java")).thenReturn(true);

		assertThrows(DuplicateResourceException.class, () -> skillService.create(new SkillRequest("Java")));
	}

	@Test
	void deletesSkillAfterFindingIt() {
		Skill skill = new Skill("Java");
		when(skillRepository.findById(7L)).thenReturn(Optional.of(skill));

		skillService.delete(7L);

		verify(skillRepository).delete(skill);
	}
}