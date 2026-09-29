package com.example.skillmatrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.skillmatrix.entity.Employee;
import com.example.skillmatrix.entity.EmployeeSkill;
import com.example.skillmatrix.entity.Project;
import com.example.skillmatrix.entity.ProjectSkillRequirement;
import com.example.skillmatrix.entity.Skill;
import com.example.skillmatrix.repository.EmployeeSkillRepository;
import com.example.skillmatrix.repository.ProjectSkillRequirementRepository;
import com.example.skillmatrix.repository.SkillRepository;
import com.example.skillmatrix.service.CoverageService;
import com.example.skillmatrix.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CoverageServiceTests {

	@Mock
	private SkillRepository skillRepository;
	@Mock
	private EmployeeSkillRepository employeeSkillRepository;
	@Mock
	private ProjectService projectService;
	@Mock
	private ProjectSkillRequirementRepository requirementRepository;
	@InjectMocks
	private CoverageService coverageService;

	@Test
	void projectCoverageReportsMissingAndBelowThresholdSkillsAsGaps() {
		Project project = new Project("Project");
		Skill java = new Skill("Java");
		Skill docker = new Skill("Docker");
		setId(java, 1L);
		setId(docker, 2L);
		Employee employee = new Employee("Alice");
		when(projectService.getProject(5L)).thenReturn(project);
		when(requirementRepository.findByProjectId(5L)).thenReturn(List.of(
				new ProjectSkillRequirement(project, java, 4),
				new ProjectSkillRequirement(project, docker, 3)));
		when(employeeSkillRepository.findAll()).thenReturn(List.of(new EmployeeSkill(employee, java, 3)));

		var coverage = coverageService.forProject(5L).skills();

		assertEquals(1, coverage.get(0).employeesWithSkill());
		assertEquals(0, coverage.get(0).employeesMeetingRequirement());
		assertEquals("GAP", coverage.get(0).status());
		assertEquals(0, coverage.get(1).employeesWithSkill());
		assertEquals("GAP", coverage.get(1).status());
	}

	@Test
	void skillIdCoverageUsesAnyExistingProficiencyAsCovered() {
		Skill java = new Skill("Java");
		setId(java, 1L);
		Employee employee = new Employee("Alice");
		when(skillRepository.findById(1L)).thenReturn(Optional.of(java));
		when(employeeSkillRepository.findAll()).thenReturn(List.of(new EmployeeSkill(employee, java, 2)));

		var coverage = coverageService.forSkills(List.of(1L)).skills().get(0);

		assertEquals(1, coverage.requiredProficiency());
		assertEquals("COVERED", coverage.status());
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