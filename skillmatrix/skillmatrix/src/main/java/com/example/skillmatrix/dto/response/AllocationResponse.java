package com.example.skillmatrix.dto.response;

import com.example.skillmatrix.entity.AllocationType;

public record AllocationResponse(Long id, Long employeeId, String employeeName, Long projectId, String projectName,
		Integer allocationPercentage, AllocationType allocationType) {
}