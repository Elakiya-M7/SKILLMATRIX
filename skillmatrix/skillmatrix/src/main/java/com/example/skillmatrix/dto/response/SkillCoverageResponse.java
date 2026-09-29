package com.example.skillmatrix.dto.response;

public record SkillCoverageResponse(Long skillId, String skillName, int requiredProficiency, int employeesWithSkill,
		int employeesMeetingRequirement, int highestProficiency, String status) {
}