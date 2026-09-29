package com.example.skillmatrix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "project_skill_requirements", uniqueConstraints = @UniqueConstraint(
		name = "uk_project_skill_requirement", columnNames = { "project_id", "skill_id" }))
public class ProjectSkillRequirement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "skill_id", nullable = false)
	private Skill skill;

	@Column(nullable = false)
	private Integer requiredProficiency;

	protected ProjectSkillRequirement() {
	}

	public ProjectSkillRequirement(Project project, Skill skill, Integer requiredProficiency) {
		this.project = project;
		this.skill = skill;
		this.requiredProficiency = requiredProficiency;
	}

	public Long getId() {
		return id;
	}

	public Project getProject() {
		return project;
	}

	public Skill getSkill() {
		return skill;
	}

	public Integer getRequiredProficiency() {
		return requiredProficiency;
	}

	public void setRequiredProficiency(Integer requiredProficiency) {
		this.requiredProficiency = requiredProficiency;
	}
}