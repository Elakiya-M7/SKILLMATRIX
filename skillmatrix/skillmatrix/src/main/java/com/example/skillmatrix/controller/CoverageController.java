package com.example.skillmatrix.controller;

import java.util.List;

import com.example.skillmatrix.dto.response.CoverageResponse;
import com.example.skillmatrix.service.CoverageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoverageController {

	private final CoverageService coverageService;

	public CoverageController(CoverageService coverageService) {
		this.coverageService = coverageService;
	}

	@GetMapping("/skills/coverage")
	public CoverageResponse forSkills(@RequestParam List<Long> skillIds) {
		return coverageService.forSkills(skillIds);
	}

	@GetMapping("/projects/{projectId}/coverage")
	public CoverageResponse forProject(@PathVariable Long projectId) {
		return coverageService.forProject(projectId);
	}
}