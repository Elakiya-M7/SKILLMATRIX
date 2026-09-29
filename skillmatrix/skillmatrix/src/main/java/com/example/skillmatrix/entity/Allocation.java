package com.example.skillmatrix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "allocations")
public class Allocation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(nullable = false)
	private Integer allocationPercentage;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AllocationType allocationType;

	protected Allocation() {
	}

	public Allocation(Employee employee, Project project, Integer allocationPercentage, AllocationType allocationType) {
		this.employee = employee;
		this.project = project;
		this.allocationPercentage = allocationPercentage;
		this.allocationType = allocationType;
	}

	public Long getId() {
		return id;
	}

	public Employee getEmployee() {
		return employee;
	}

	public Project getProject() {
		return project;
	}

	public Integer getAllocationPercentage() {
		return allocationPercentage;
	}

	public AllocationType getAllocationType() {
		return allocationType;
	}

	public void setAllocationPercentage(Integer allocationPercentage) {
		this.allocationPercentage = allocationPercentage;
	}

	public void setAllocationType(AllocationType allocationType) {
		this.allocationType = allocationType;
	}
}