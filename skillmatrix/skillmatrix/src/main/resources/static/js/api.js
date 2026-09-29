(() => {
  const fallbackMessages = {
    400: "Please check the entered data.",
    404: "The requested resource was not found.",
    409: "The operation conflicts with existing data or employee capacity.",
    500: "Server error. Please try again."
  };

  function errorMessage(data, status) {
    if (typeof data?.message === "string" && data.message.trim()) return data.message;

    const fieldErrors = data?.fieldErrors;
    if (fieldErrors && typeof fieldErrors === "object") {
      const messages = Object.values(fieldErrors).flat().filter((message) => typeof message === "string" && message.trim());
      if (messages.length) return messages.join("; ");
    }

    if (Array.isArray(data?.errors)) {
      const messages = data.errors.map((item) => typeof item === "string" ? item : item?.defaultMessage || item?.message)
        .filter((message) => typeof message === "string" && message.trim());
      if (messages.length) return messages.join("; ");
    }

    return fallbackMessages[status] || `Request failed (${status}).`;
  }

  async function request(path, options = {}) {
    const requestOptions = {
      method: options.method || "GET",
      headers: { Accept: "application/json" }
    };

    if (options.body !== undefined) {
      requestOptions.headers["Content-Type"] = "application/json";
      requestOptions.body = JSON.stringify(options.body);
    }

    let response;
    try {
      response = await fetch(path, requestOptions);
    } catch {
      throw new Error("Cannot connect to SkillMatrix. Check that the server is running.");
    }

    if (response.status === 204) return null;

    const text = await response.text();
    let data = null;
    if (text) {
      try {
        data = JSON.parse(text);
      } catch {
        data = null;
      }
    }

    if (!response.ok) {
      const error = new Error(errorMessage(data, response.status));
      error.status = response.status;
      throw error;
    }

    return data;
  }

  const id = (value) => encodeURIComponent(value);
  const jsonRequest = (method, body) => ({ method, body });
  const getProjectRecommendations = (projectId) => request(`/projects/${id(projectId)}/recommendations`);

  window.skillMatrixApi = Object.freeze({
    getEmployees: () => request("/employees"),
    getEmployee: (employeeId) => request(`/employees/${id(employeeId)}`),
    createEmployee: (data) => request("/employees", jsonRequest("POST", data)),
    updateEmployee: (employeeId, data) => request(`/employees/${id(employeeId)}`, jsonRequest("PUT", data)),
    deleteEmployee: (employeeId) => request(`/employees/${id(employeeId)}`, { method: "DELETE" }),

    getEmployeeSkills: (employeeId) => request(`/employees/${id(employeeId)}/skills`),
    addEmployeeSkill: (employeeId, data) => request(`/employees/${id(employeeId)}/skills`, jsonRequest("POST", data)),
    updateEmployeeSkill: (employeeId, skillId, data) => request(`/employees/${id(employeeId)}/skills/${id(skillId)}`, jsonRequest("PUT", data)),
    deleteEmployeeSkill: (employeeId, skillId) => request(`/employees/${id(employeeId)}/skills/${id(skillId)}`, { method: "DELETE" }),

    getSkills: () => request("/skills"),
    getSkill: (skillId) => request(`/skills/${id(skillId)}`),
    createSkill: (data) => request("/skills", jsonRequest("POST", data)),
    updateSkill: (skillId, data) => request(`/skills/${id(skillId)}`, jsonRequest("PUT", data)),
    deleteSkill: (skillId) => request(`/skills/${id(skillId)}`, { method: "DELETE" }),

    getProjects: () => request("/projects"),
    getProject: (projectId) => request(`/projects/${id(projectId)}`),
    createProject: (data) => request("/projects", jsonRequest("POST", data)),
    updateProject: (projectId, data) => request(`/projects/${id(projectId)}`, jsonRequest("PUT", data)),
    deleteProject: (projectId) => request(`/projects/${id(projectId)}`, { method: "DELETE" }),

    getProjectRequirements: (projectId) => request(`/projects/${id(projectId)}/requirements`),
    addProjectRequirement: (projectId, data) => request(`/projects/${id(projectId)}/requirements`, jsonRequest("POST", data)),
    updateProjectRequirement: (projectId, skillId, data) => request(`/projects/${id(projectId)}/requirements/${id(skillId)}`, jsonRequest("PUT", data)),
    deleteProjectRequirement: (projectId, skillId) => request(`/projects/${id(projectId)}/requirements/${id(skillId)}`, { method: "DELETE" }),

    createAllocation: (projectId, data) => request(`/projects/${id(projectId)}/allocations`, jsonRequest("POST", data)),
    updateAllocation: (allocationId, data) => request(`/allocations/${id(allocationId)}`, jsonRequest("PUT", data)),
    deleteAllocation: (allocationId) => request(`/allocations/${id(allocationId)}`, { method: "DELETE" }),
    getEmployeeAllocations: (employeeId) => request(`/employees/${id(employeeId)}/allocations`),
    getProjectAllocations: (projectId) => request(`/projects/${id(projectId)}/allocations`),

    getProjectRecommendations,
    getRecommendations: getProjectRecommendations,
    getSkillCoverage: (skillIds) => request(`/skills/coverage?skillIds=${encodeURIComponent(skillIds.join(","))}`),
    getProjectCoverage: (projectId) => request(`/projects/${id(projectId)}/coverage`)
  });
})();