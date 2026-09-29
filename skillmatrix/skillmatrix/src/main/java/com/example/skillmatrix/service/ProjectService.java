package com.example.skillmatrix.service;

import java.util.List;

import com.example.skillmatrix.dto.request.ProjectRequest;
import com.example.skillmatrix.dto.response.ProjectResponse;
import com.example.skillmatrix.entity.Project;
import com.example.skillmatrix.exception.ResourceNotFoundException;
import com.example.skillmatrix.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

	private final ProjectRepository projectRepository;

	public ProjectService(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Transactional
	public ProjectResponse create(ProjectRequest request) {
		return toResponse(projectRepository.save(new Project(request.name())));
	}

	@Transactional(readOnly = true)
	public List<ProjectResponse> findAll() {
		return projectRepository.findAll().stream().map(ProjectService::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public ProjectResponse findById(Long id) {
		return toResponse(getProject(id));
	}

	@Transactional
	public ProjectResponse update(Long id, ProjectRequest request) {
		Project project = getProject(id);
		project.setName(request.name());
		return toResponse(projectRepository.save(project));
	}

	@Transactional
	public void delete(Long id) {
		projectRepository.delete(getProject(id));
	}

	public Project getProject(Long id) {
		return projectRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Project with id " + id + " not found"));
	}

	private static ProjectResponse toResponse(Project project) {
		return new ProjectResponse(project.getId(), project.getName());
	}
}