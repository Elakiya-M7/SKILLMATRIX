package com.example.skillmatrix.controller;

import java.util.List;

import com.example.skillmatrix.dto.request.SkillRequest;
import com.example.skillmatrix.dto.response.SkillResponse;
import com.example.skillmatrix.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/skills")
public class SkillController {

	private final SkillService skillService;

	public SkillController(SkillService skillService) {
		this.skillService = skillService;
	}

	@PostMapping
	public ResponseEntity<SkillResponse> create(@Valid @RequestBody SkillRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(skillService.create(request));
	}

	@GetMapping
	public List<SkillResponse> findAll() {
		return skillService.findAll();
	}

	@GetMapping("/{id}")
	public SkillResponse findById(@PathVariable Long id) {
		return skillService.findById(id);
	}

	@PutMapping("/{id}")
	public SkillResponse update(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
		return skillService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		skillService.delete(id);
		return ResponseEntity.noContent().build();
	}
}