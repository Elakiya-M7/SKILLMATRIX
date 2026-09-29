package com.example.skillmatrix.service;

import java.util.List;

import com.example.skillmatrix.dto.request.EmployeeSkillRequest;
import com.example.skillmatrix.dto.request.ProficiencyRequest;
import com.example.skillmatrix.dto.response.EmployeeSkillResponse;
import com.example.skillmatrix.entity.Employee;
import com.example.skillmatrix.entity.EmployeeSkill;
import com.example.skillmatrix.entity.Skill;
import com.example.skillmatrix.exception.DuplicateResourceException;
import com.example.skillmatrix.exception.InvalidRequestException;
import com.example.skillmatrix.exception.ResourceNotFoundException;
import com.example.skillmatrix.repository.EmployeeRepository;
import com.example.skillmatrix.repository.EmployeeSkillRepository;
import com.example.skillmatrix.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeSkillService {

	private final EmployeeRepository employeeRepository;
	private final SkillRepository skillRepository;
	private final EmployeeSkillRepository employeeSkillRepository;

	public EmployeeSkillService(
			EmployeeRepository employeeRepository,
			SkillRepository skillRepository,
			EmployeeSkillRepository employeeSkillRepository) {
		this.employeeRepository = employeeRepository;
		this.skillRepository = skillRepository;
		this.employeeSkillRepository = employeeSkillRepository;
	}

	@Transactional
	public EmployeeSkillResponse add(Long employeeId, EmployeeSkillRequest request) {
		validateProficiency(request.proficiency());
		Employee employee = getEmployee(employeeId);
		Skill skill = getSkill(request.skillId());
		if (employeeSkillRepository.existsByEmployeeIdAndSkillId(employeeId, request.skillId())) {
			throw new DuplicateResourceException("Employee already has skill with id " + request.skillId());
		}
		return toResponse(employeeSkillRepository.save(new EmployeeSkill(employee, skill, request.proficiency())));
	}

	@Transactional
	public EmployeeSkillResponse update(Long employeeId, Long skillId, ProficiencyRequest request) {
		validateProficiency(request.proficiency());
		getEmployee(employeeId);
		EmployeeSkill employeeSkill = findRelationship(employeeId, skillId);
		employeeSkill.setProficiency(request.proficiency());
		return toResponse(employeeSkillRepository.save(employeeSkill));
	}

	@Transactional(readOnly = true)
	public List<EmployeeSkillResponse> findByEmployee(Long employeeId) {
		getEmployee(employeeId);
		return employeeSkillRepository.findByEmployeeId(employeeId).stream()
				.map(EmployeeSkillService::toResponse).toList();
	}

	@Transactional
	public void delete(Long employeeId, Long skillId) {
		getEmployee(employeeId);
		employeeSkillRepository.delete(findRelationship(employeeId, skillId));
	}

	private Employee getEmployee(Long id) {
		return employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Employee with id " + id + " not found"));
	}

	private Skill getSkill(Long id) {
		return skillRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Skill with id " + id + " not found"));
	}

	private EmployeeSkill findRelationship(Long employeeId, Long skillId) {
		return employeeSkillRepository.findByEmployeeIdAndSkillId(employeeId, skillId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Employee " + employeeId + " does not have skill " + skillId));
	}

	private void validateProficiency(Integer proficiency) {
		if (proficiency == null || proficiency < 1 || proficiency > 5) {
			throw new InvalidRequestException("proficiency must be between 1 and 5");
		}
	}

	private static EmployeeSkillResponse toResponse(EmployeeSkill employeeSkill) {
		return new EmployeeSkillResponse(employeeSkill.getSkill().getId(), employeeSkill.getSkill().getName(),
				employeeSkill.getProficiency());
	}
}