package com.example.skillmatrix.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.skillmatrix.dto.response.RecommendationResponse;
import com.example.skillmatrix.dto.response.SkillMatchResponse;
import com.example.skillmatrix.entity.Allocation;
import com.example.skillmatrix.entity.Employee;
import com.example.skillmatrix.entity.EmployeeSkill;
import com.example.skillmatrix.entity.ProjectSkillRequirement;
import com.example.skillmatrix.exception.InvalidRequestException;
import com.example.skillmatrix.repository.AllocationRepository;
import com.example.skillmatrix.repository.EmployeeRepository;
import com.example.skillmatrix.repository.EmployeeSkillRepository;
import com.example.skillmatrix.repository.ProjectSkillRequirementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {

	private final ProjectService projectService;
	private final ProjectSkillRequirementRepository requirementRepository;
	private final EmployeeRepository employeeRepository;
	private final EmployeeSkillRepository employeeSkillRepository;
	private final AllocationRepository allocationRepository;

	public RecommendationService(ProjectService projectService,
			ProjectSkillRequirementRepository requirementRepository, EmployeeRepository employeeRepository,
			EmployeeSkillRepository employeeSkillRepository, AllocationRepository allocationRepository) {
		this.projectService = projectService;
		this.requirementRepository = requirementRepository;
		this.employeeRepository = employeeRepository;
		this.employeeSkillRepository = employeeSkillRepository;
		this.allocationRepository = allocationRepository;
	}

	@Transactional(readOnly = true)
	public List<RecommendationResponse> recommend(Long projectId) {
		projectService.getProject(projectId);
		List<ProjectSkillRequirement> requirements = requirementRepository.findByProjectId(projectId);
		if (requirements.isEmpty()) {
			throw new InvalidRequestException("Project " + projectId + " has no skill requirements");
		}

		return employeeRepository.findAll().stream()
				.map(employee -> scoreEmployee(employee, requirements))
				.filter(recommendation -> recommendation.availableCapacity() > 0)
				.sorted(Comparator.comparingInt(RecommendationResponse::matchedSkillCount).reversed()
						.thenComparing(Comparator.comparingInt(RecommendationResponse::proficiencyScore).reversed())
						.thenComparing(Comparator.comparingInt(RecommendationResponse::availableCapacity).reversed()))
				.toList();
	}

	private RecommendationResponse scoreEmployee(Employee employee, List<ProjectSkillRequirement> requirements) {
		Map<Long, EmployeeSkill> skillsById = new HashMap<>();
		for (EmployeeSkill employeeSkill : employeeSkillRepository.findByEmployeeId(employee.getId())) {
			skillsById.put(employeeSkill.getSkill().getId(), employeeSkill);
		}

		List<SkillMatchResponse> matchedSkills = requirements.stream()
				.map(requirement -> skillsById.get(requirement.getSkill().getId()))
				.filter(employeeSkill -> employeeSkill != null)
				.map(employeeSkill -> new SkillMatchResponse(employeeSkill.getSkill().getId(),
						employeeSkill.getSkill().getName(), employeeSkill.getProficiency()))
				.toList();
		List<String> missingSkills = requirements.stream()
				.filter(requirement -> !skillsById.containsKey(requirement.getSkill().getId()))
				.map(requirement -> requirement.getSkill().getName())
				.toList();
		int proficiencyScore = matchedSkills.stream().mapToInt(SkillMatchResponse::proficiency).sum();
		int currentAllocation = allocationRepository.findByEmployeeId(employee.getId()).stream()
				.mapToInt(Allocation::getAllocationPercentage).sum();
		int availableCapacity = Math.max(0, 100 - currentAllocation);

		return new RecommendationResponse(employee.getId(), employee.getName(), matchedSkills, missingSkills,
				matchedSkills.size(), proficiencyScore, availableCapacity);
	}
}