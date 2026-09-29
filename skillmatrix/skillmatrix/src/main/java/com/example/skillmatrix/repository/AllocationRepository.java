package com.example.skillmatrix.repository;

import java.util.List;

import com.example.skillmatrix.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
	List<Allocation> findByEmployeeId(Long employeeId);

	List<Allocation> findByProjectId(Long projectId);
}