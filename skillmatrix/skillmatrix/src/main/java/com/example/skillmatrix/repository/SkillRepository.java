package com.example.skillmatrix.repository;



import com.example.skillmatrix.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);
}