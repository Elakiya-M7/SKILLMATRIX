package com.example.skillmatrix.service;

import java.util.ArrayList;
import java.util.List;

import com.example.skillmatrix.dto.response.CoverageResponse;
import com.example.skillmatrix.dto.response.SkillCoverageResponse;
import com.example.skillmatrix.entity.EmployeeSkill;
import com.example.skillmatrix.entity.ProjectSkillRequirement;
import com.example.skillmatrix.entity.Skill;
import com.example.skillmatrix.exception.InvalidRequestException;
import com.example.skillmatrix.exception.ResourceNotFoundException;
import com.example.skillmatrix.repository.EmployeeSkillRepository;
import com.example.skillmatrix.repository.ProjectSkillRequirementRepository;
import com.example.skillmatrix.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoverageService {

	private final SkillRepository skillRepository;
	private final EmployeeSkillRepository employeeSkillRepository;
	private final ProjectService projectService;
	private final ProjectSkillRequirementRepository requirementRepository;

	public CoverageService(SkillRepository skillRepository, EmployeeSkillRepository employeeSkillRepository,
			ProjectService projectService, ProjectSkillRequirementRepository requirementRepository) {
		this.skillRepository = skillRepository;
		this.employeeSkillRepository = employeeSkillRepository;
		this.projectService = projectService;
		this.requirementRepository = requirementRepository;
	}

	@Transactional(readOnly = true)
	public CoverageResponse forSkills(List<Long> skillIds) {
		if (skillIds == null || skillIds.isEmpty()) {
			throw new InvalidRequestException("At least one skillId is required");
		}
		List<SkillCoverageResponse> coverage = new ArrayList<>();
		for (Long skillId : skillIds) {
			Skill skill = skillRepository.findById(skillId)
					.orElseThrow(() -> new ResourceNotFoundException("Skill with id " + skillId + " not found"));
			coverage.add(calculate(skill, 1));
		}
		return new CoverageResponse(coverage);
	}

	@Transactional(readOnly = true)
	public CoverageResponse forProject(Long projectId) {
		projectService.getProject(projectId);
		List<ProjectSkillRequirement> requirements = requirementRepository.findByProjectId(projectId);
		return new CoverageResponse(requirements.stream()
				.map(requirement -> calculate(requirement.getSkill(), requirement.getRequiredProficiency()))
				.toList());
	}

	private SkillCoverageResponse calculate(Skill skill, int requiredProficiency) {
		List<EmployeeSkill> holders = employeeSkillRepository.findAll().stream()
				.filter(employeeSkill -> employeeSkill.getSkill().getId().equals(skill.getId()))
				.toList();
		int employeesMeetingRequirement = (int) holders.stream()
				.filter(employeeSkill -> employeeSkill.getProficiency() >= requiredProficiency).count();
		int highestProficiency = holders.stream().mapToInt(EmployeeSkill::getProficiency).max().orElse(0);
		String status = employeesMeetingRequirement == 0 ? "GAP" : "COVERED";
		return new SkillCoverageResponse(skill.getId(), skill.getName(), requiredProficiency, holders.size(),
				employeesMeetingRequirement, highestProficiency, status);
	}
}