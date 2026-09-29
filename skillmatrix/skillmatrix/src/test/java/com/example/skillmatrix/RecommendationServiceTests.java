package com.example.skillmatrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;

import com.example.skillmatrix.entity.Allocation;
import com.example.skillmatrix.entity.AllocationType;
import com.example.skillmatrix.entity.Employee;
import com.example.skillmatrix.entity.EmployeeSkill;
import com.example.skillmatrix.entity.Project;
import com.example.skillmatrix.entity.ProjectSkillRequirement;
import com.example.skillmatrix.entity.Skill;
import com.example.skillmatrix.exception.InvalidRequestException;
import com.example.skillmatrix.repository.AllocationRepository;
import com.example.skillmatrix.repository.EmployeeRepository;
import com.example.skillmatrix.repository.EmployeeSkillRepository;
import com.example.skillmatrix.repository.ProjectSkillRequirementRepository;
import com.example.skillmatrix.service.ProjectService;
import com.example.skillmatrix.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTests {

	@Mock
	private ProjectService projectService;
	@Mock
	private ProjectSkillRequirementRepository requirementRepository;
	@Mock
	private EmployeeRepository employeeRepository;
	@Mock
	private EmployeeSkillRepository employeeSkillRepository;
	@Mock
	private AllocationRepository allocationRepository;
	@InjectMocks
	private RecommendationService recommendationService;

	@Test
	void ranksHigherProficiencyFirstAndExcludesFullyAllocatedEmployee() {
		Project project = new Project("Project");
		Skill java = new Skill("Java");
		setId(java, 3L);
		Employee lower = new Employee("Lower");
		Employee full = new Employee("Full");
		Employee higher = new Employee("Higher");
		setId(lower, 1L);
		setId(full, 2L);
		setId(higher, 3L);
		when(projectService.getProject(9L)).thenReturn(project);
		when(requirementRepository.findByProjectId(9L))
				.thenReturn(List.of(new ProjectSkillRequirement(project, java, 3)));
		when(employeeRepository.findAll()).thenReturn(List.of(lower, full, higher));
		when(employeeSkillRepository.findByEmployeeId(1L)).thenReturn(List.of(new EmployeeSkill(lower, java, 3)));
		when(employeeSkillRepository.findByEmployeeId(2L)).thenReturn(List.of(new EmployeeSkill(full, java, 4)));
		when(employeeSkillRepository.findByEmployeeId(3L)).thenReturn(List.of(new EmployeeSkill(higher, java, 5)));
		when(allocationRepository.findByEmployeeId(1L)).thenReturn(List.of());
		when(allocationRepository.findByEmployeeId(2L))
				.thenReturn(List.of(new Allocation(full, project, 100, AllocationType.FULL)));
		when(allocationRepository.findByEmployeeId(3L)).thenReturn(List.of());

		var recommendations = recommendationService.recommend(9L);

		assertEquals(List.of(3L, 1L), recommendations.stream().map(response -> response.employeeId()).toList());
		assertEquals(5, recommendations.get(0).proficiencyScore());
	}

	@Test
	void rejectsProjectWithoutRequirements() {
		when(projectService.getProject(9L)).thenReturn(new Project("Project"));
		when(requirementRepository.findByProjectId(9L)).thenReturn(List.of());

		assertThrows(InvalidRequestException.class, () -> recommendationService.recommend(9L));
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