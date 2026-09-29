package com.example.skillmatrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.skillmatrix.dto.request.AllocationRequest;
import com.example.skillmatrix.dto.request.EmployeeRequest;
import com.example.skillmatrix.dto.request.EmployeeSkillRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
class SkillmatrixApplicationTests {

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void acceptsCorrectJsonTypesAndRejectsCoercion() throws JacksonException {
		assertEquals("12", jsonMapper.readValue("{\"name\":\"12\"}", EmployeeRequest.class).name());
		assertThrows(JacksonException.class,
				() -> jsonMapper.readValue("{\"name\":12}", EmployeeRequest.class));
		assertThrows(JacksonException.class,
				() -> jsonMapper.readValue("{\"skillId\":1,\"proficiency\":\"4\"}", EmployeeSkillRequest.class));
		assertThrows(JacksonException.class,
				() -> jsonMapper.readValue(
						"{\"employeeId\":1,\"allocationPercentage\":25,\"allocationType\":0}",
						AllocationRequest.class));
	}

}
