package com.example.skillmatrix.service;

import java.util.List;

import com.example.skillmatrix.dto.request.SkillRequest;
import com.example.skillmatrix.dto.response.SkillResponse;
import com.example.skillmatrix.entity.Skill;
import com.example.skillmatrix.exception.DuplicateResourceException;
import com.example.skillmatrix.exception.ResourceNotFoundException;
import com.example.skillmatrix.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillService {

	private final SkillRepository skillRepository;

	public SkillService(SkillRepository skillRepository) {
		this.skillRepository = skillRepository;
	}

	@Transactional
	public SkillResponse create(SkillRequest request) {
		if (skillRepository.existsByName(request.name())) {
			throw new DuplicateResourceException("Skill with name '" + request.name() + "' already exists");
		}
		return toResponse(skillRepository.save(new Skill(request.name())));
	}

	@Transactional(readOnly = true)
	public List<SkillResponse> findAll() {
		return skillRepository.findAll().stream().map(SkillService::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public SkillResponse findById(Long id) {
		return toResponse(getSkill(id));
	}

	@Transactional
	public SkillResponse update(Long id, SkillRequest request) {
		Skill skill = getSkill(id);
		if (skillRepository.existsByNameAndIdNot(request.name(), id)) {
			throw new DuplicateResourceException("Skill with name '" + request.name() + "' already exists");
		}
		skill.setName(request.name());
		return toResponse(skillRepository.save(skill));
	}

	@Transactional
	public void delete(Long id) {
		skillRepository.delete(getSkill(id));
	}

	private Skill getSkill(Long id) {
		return skillRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Skill with id " + id + " not found"));
	}

	private static SkillResponse toResponse(Skill skill) {
		return new SkillResponse(skill.getId(), skill.getName());
	}
}