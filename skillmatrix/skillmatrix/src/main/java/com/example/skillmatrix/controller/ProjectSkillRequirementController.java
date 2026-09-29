package com.example.skillmatrix.controller;

import java.util.List;

import com.example.skillmatrix.dto.request.ProjectSkillRequirementRequest;
import com.example.skillmatrix.dto.request.RequiredProficiencyRequest;
import com.example.skillmatrix.dto.response.ProjectSkillRequirementResponse;
import com.example.skillmatrix.service.ProjectSkillRequirementService;
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
@RequestMapping("/projects/{projectId}/requirements")
public class ProjectSkillRequirementController {

	private final ProjectSkillRequirementService requirementService;

	public ProjectSkillRequirementController(ProjectSkillRequirementService requirementService) {
		this.requirementService = requirementService;
	}

	@PostMapping
	public ResponseEntity<ProjectSkillRequirementResponse> add(@PathVariable Long projectId,
			@Valid @RequestBody ProjectSkillRequirementRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(requirementService.add(projectId, request));
	}

	@GetMapping
	public List<ProjectSkillRequirementResponse> findByProject(@PathVariable Long projectId) {
		return requirementService.findByProject(projectId);
	}

	@PutMapping("/{skillId}")
	public ProjectSkillRequirementResponse update(@PathVariable Long projectId, @PathVariable Long skillId,
			@Valid @RequestBody RequiredProficiencyRequest request) {
		return requirementService.update(projectId, skillId, request);
	}

	@DeleteMapping("/{skillId}")
	public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long skillId) {
		requirementService.delete(projectId, skillId);
		return ResponseEntity.noContent().build();
	}
}