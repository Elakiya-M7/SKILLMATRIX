package com.example.skillmatrix.repository;

import java.util.List;
import java.util.Optional;

import com.example.skillmatrix.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
	List<EmployeeSkill> findByEmployeeId(Long employeeId);

	Optional<EmployeeSkill> findByEmployeeIdAndSkillId(Long employeeId, Long skillId);

	boolean existsByEmployeeIdAndSkillId(Long employeeId, Long skillId);
}