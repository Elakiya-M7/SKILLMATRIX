package com.example.skillmatrix.repository;

import com.example.skillmatrix.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}