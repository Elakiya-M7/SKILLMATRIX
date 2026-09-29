package com.example.skillmatrix.controller;

import java.util.List;

import com.example.skillmatrix.dto.response.RecommendationResponse;
import com.example.skillmatrix.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/recommendations")
public class RecommendationController {

	private final RecommendationService recommendationService;

	public RecommendationController(RecommendationService recommendationService) {
		this.recommendationService = recommendationService;
	}

	@GetMapping
	public List<RecommendationResponse> recommend(@PathVariable Long projectId) {
		return recommendationService.recommend(projectId);
	}
}