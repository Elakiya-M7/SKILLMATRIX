package com.example.skillmatrix.dto.response;

import java.util.List;

public record RecommendationResponse(Long employeeId, String employeeName, List<SkillMatchResponse> matchedSkills,
		List<String> missingSkills, int matchedSkillCount, int proficiencyScore, int availableCapacity) {
}