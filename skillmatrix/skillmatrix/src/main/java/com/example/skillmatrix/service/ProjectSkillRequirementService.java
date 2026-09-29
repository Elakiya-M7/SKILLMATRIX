package com.example.skillmatrix.service;

import java.util.List;

import com.example.skillmatrix.dto.request.ProjectSkillRequirementRequest;
import com.example.skillmatrix.dto.request.RequiredProficiencyRequest;
import com.example.skillmatrix.dto.response.ProjectSkillRequirementResponse;
import com.example.skillmatrix.entity.Project;
import com.example.skillmatrix.entity.ProjectSkillRequirement;
import com.example.skillmatrix.entity.Skill;
import com.example.skillmatrix.exception.DuplicateResourceException;
import com.example.skillmatrix.exception.InvalidRequestException;
import com.example.skillmatrix.exception.ResourceNotFoundException;
import com.example.skillmatrix.repository.ProjectSkillRequirementRepository;
import com.example.skillmatrix.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectSkillRequirementService {

	private final ProjectService projectService;
	private final SkillRepository skillRepository;
	private final ProjectSkillRequirementRepository requirementRepository;

	public ProjectSkillRequirementService(ProjectService projectService, SkillRepository skillRepository,
			ProjectSkillRequirementRepository requirementRepository) {
		this.projectService = projectService;
		this.skillRepository = skillRepository;
		this.requirementRepository = requirementRepository;
	}

	@Transactional
	public ProjectSkillRequirementResponse add(Long projectId, ProjectSkillRequirementRequest request) {
		validateProficiency(request.requiredProficiency());
		Project project = projectService.getProject(projectId);
		Skill skill = skillRepository.findById(request.skillId())
				.orElseThrow(() -> new ResourceNotFoundException("Skill with id " + request.skillId() + " not found"));
		if (requirementRepository.existsByProjectIdAndSkillId(projectId, request.skillId())) {
			throw new DuplicateResourceException("Project already requires skill with id " + request.skillId());
		}
		return toResponse(requirementRepository.save(
				new ProjectSkillRequirement(project, skill, request.requiredProficiency())));
	}

	@Transactional(readOnly = true)
	public List<ProjectSkillRequirementResponse> findByProject(Long projectId) {
		projectService.getProject(projectId);
		return requirementRepository.findByProjectId(projectId).stream()
				.map(ProjectSkillRequirementService::toResponse).toList();
	}

	@Transactional
	public ProjectSkillRequirementResponse update(Long projectId, Long skillId,
			RequiredProficiencyRequest request) {
		validateProficiency(request.requiredProficiency());
		projectService.getProject(projectId);
		ProjectSkillRequirement requirement = requirementRepository.findByProjectIdAndSkillId(projectId, skillId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Project " + projectId + " does not require skill " + skillId));
		requirement.setRequiredProficiency(request.requiredProficiency());
		return toResponse(requirementRepository.save(requirement));
	}

	@Transactional
	public void delete(Long projectId, Long skillId) {
		projectService.getProject(projectId);
		ProjectSkillRequirement requirement = requirementRepository.findByProjectIdAndSkillId(projectId, skillId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Project " + projectId + " does not require skill " + skillId));
		requirementRepository.delete(requirement);
	}

	private void validateProficiency(Integer proficiency) {
		if (proficiency == null || proficiency < 1 || proficiency > 5) {
			throw new InvalidRequestException("requiredProficiency must be between 1 and 5");
		}
	}

	private static ProjectSkillRequirementResponse toResponse(ProjectSkillRequirement requirement) {
		return new ProjectSkillRequirementResponse(requirement.getSkill().getId(), requirement.getSkill().getName(),
				requirement.getRequiredProficiency());
	}
}