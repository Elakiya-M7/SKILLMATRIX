package com.example.skillmatrix.repository;



import com.example.skillmatrix.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}