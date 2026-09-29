package com.example.skillmatrix.repository;

import java.util.List;
import java.util.Optional;

import com.example.skillmatrix.entity.ProjectSkillRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectSkillRequirementRepository extends JpaRepository<ProjectSkillRequirement, Long> {
	List<ProjectSkillRequirement> findByProjectId(Long projectId);

	Optional<ProjectSkillRequirement> findByProjectIdAndSkillId(Long projectId, Long skillId);

	boolean existsByProjectIdAndSkillId(Long projectId, Long skillId);
}