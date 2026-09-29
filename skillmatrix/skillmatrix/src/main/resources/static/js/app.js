(() => {
  const $ = (selector, root = document) => root.querySelector(selector);
  const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
  const api = window.skillMatrixApi;
  const state = { employees: [], skills: [], projects: [], charts: {}, dialog: null, currentView: "dashboard" };
  const navButtons = $$(".nav-link");
  const views = $$(".view-panel");

  const feedback = (selector, message = "", isError = false) => {
    const element = $(selector);
    if (!element) return;
    element.textContent = message;
    element.hidden = !message;
    element.classList.toggle("is-error", isError);
    element.setAttribute("role", isError ? "alert" : "status");
  };

  function tableMessage(body, colspan, message) {
    const row = document.createElement("tr");
    const cell = document.createElement("td");
    cell.colSpan = colspan;
    cell.className = "table-message";
    cell.textContent = message;
    row.append(cell);
    body.replaceChildren(row);
  }

  function actionButton(label, action, id, destructive = false) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `employee-action-button${destructive ? " is-danger" : ""}`;
    button.textContent = label;
    button.dataset.action = action;
    button.dataset.id = id;
    return button;
  }

  function actionCell(actions) {
    const cell = document.createElement("td");
    const wrapper = document.createElement("div");
    wrapper.className = "employee-actions";
    wrapper.append(...actions);
    cell.append(wrapper);
    return cell;
  }

  function appendRow(body, cells) {
    const row = document.createElement("tr");
    row.append(...cells);
    body.append(row);
  }

  function textCell(value) {
    const cell = document.createElement("td");
    cell.textContent = value ?? "";
    return cell;
  }

  function fillSelect(select, records, { placeholder = "Select", valueKey = "id", labelKey = "name", selected = "" } = {}) {
    const options = [new Option(placeholder, "")];
    for (const record of records) options.push(new Option(record[labelKey], record[valueKey]));
    select.replaceChildren(...options);
    if (selected !== "" && records.some((record) => String(record[valueKey]) === String(selected))) select.value = String(selected);
  }

  function refreshSelectors() {
    const config = [
      ["#employee-skill-employee", state.employees, "Select employee"],
      ["#allocation-employee", state.employees, "Select employee"],
      ["#allocation-list-employee", state.employees, "Select employee"],
      ["#requirement-project", state.projects, "Select project"],
      ["#recommendation-project", state.projects, "Select project"],
      ["#allocation-project", state.projects, "Select project"],
      ["#allocation-list-project", state.projects, "Select project"],
      ["#coverage-project-select", state.projects, "Select project"]
    ];
    for (const [selector, records, placeholder] of config) {
      const select = $(selector);
      const previous = select.value;
      fillSelect(select, records, { placeholder, selected: previous });
    }
    const skillsSelect = $("#coverage-skill-select");
    const selectedSkills = new Set([...skillsSelect.selectedOptions].map((option) => option.value));
    fillSelect(skillsSelect, state.skills, { placeholder: "Select skills", selected: "" });
    for (const option of skillsSelect.options) if (selectedSkills.has(option.value)) option.selected = true;
    $("#load-recommendations-button").disabled = state.projects.length === 0;
    $("#load-project-coverage-button").disabled = state.projects.length === 0;
  }

  async function loadEmployees({ silent = false } = {}) {
    if (!silent) tableMessage($("#employee-table-body"), 3, "Loading employees...");
    try {
      state.employees = await api.getEmployees();
      $("#employee-count").textContent = `${state.employees.length} ${state.employees.length === 1 ? "employee" : "employees"}`;
      const body = $("#employee-table-body");
      if (!state.employees.length) tableMessage(body, 3, "No employees found. Add your first employee.");
      else {
        body.replaceChildren();
        for (const employee of state.employees) appendRow(body, [
          textCell(employee.id), textCell(employee.name),
          actionCell([actionButton("Edit", "edit-employee", employee.id), actionButton("Delete", "delete-employee", employee.id, true)])
        ]);
      }
      refreshSelectors();
      feedback("#employee-feedback");
      return state.employees;
    } catch (error) {
      $("#employee-count").textContent = "";
      tableMessage($("#employee-table-body"), 3, "Employees could not be loaded.");
      feedback("#employee-feedback", error.message || "Unable to connect to the server. Make sure Spring Boot is running.", true);
      throw error;
    }
  }

  async function loadSkills({ silent = false } = {}) {
    if (!silent) tableMessage($("#skill-table-body"), 3, "Loading skills...");
    try {
      state.skills = await api.getSkills();
      $("#skill-count").textContent = `${state.skills.length} ${state.skills.length === 1 ? "skill" : "skills"}`;
      const body = $("#skill-table-body");
      if (!state.skills.length) tableMessage(body, 3, "No skills found. Add your first skill.");
      else {
        body.replaceChildren();
        for (const skill of state.skills) appendRow(body, [
          textCell(skill.id), textCell(skill.name),
          actionCell([actionButton("Edit", "edit-skill", skill.id), actionButton("Delete", "delete-skill", skill.id, true)])
        ]);
      }
      refreshSelectors();
      feedback("#skill-feedback");
      return state.skills;
    } catch (error) {
      $("#skill-count").textContent = "";
      tableMessage($("#skill-table-body"), 3, "Skills could not be loaded.");
      feedback("#skill-feedback", error.message, true);
      throw error;
    }
  }

  async function loadProjects({ silent = false } = {}) {
    if (!silent) tableMessage($("#project-table-body"), 3, "Loading projects...");
    try {
      state.projects = await api.getProjects();
      $("#project-count").textContent = `${state.projects.length} ${state.projects.length === 1 ? "project" : "projects"}`;
      const body = $("#project-table-body");
      if (!state.projects.length) tableMessage(body, 3, "No projects found. Create a project to get started.");
      else {
        body.replaceChildren();
        for (const project of state.projects) appendRow(body, [
          textCell(project.id), textCell(project.name),
          actionCell([actionButton("Edit", "edit-project", project.id), actionButton("Delete", "delete-project", project.id, true)])
        ]);
      }
      refreshSelectors();
      feedback("#project-feedback");
      return state.projects;
    } catch (error) {
      $("#project-count").textContent = "";
      tableMessage($("#project-table-body"), 3, "Projects could not be loaded.");
      feedback("#project-feedback", error.message, true);
      throw error;
    }
  }

  async function loadReferenceData() {
    const results = await Promise.allSettled([loadEmployees({ silent: true }), loadSkills({ silent: true }), loadProjects({ silent: true })]);
    const failed = results.find((result) => result.status === "rejected");
    if (failed) throw failed.reason;
  }

  function showDialogError(message) {
    const error = $("#crud-form-error");
    error.textContent = message || "Please check the entered data.";
    error.hidden = false;
  }

  function makeField(spec) {
    const wrapper = document.createElement("label");
    wrapper.className = "form-field dialog-field";
    const label = document.createElement("span");
    label.textContent = spec.label;
    const control = spec.type === "select" ? document.createElement("select") : document.createElement("input");
    control.name = spec.name;
    control.id = `crud-${spec.name}`;
    control.className = "control-input";
    if (spec.type === "select") {
      control.classList.add("control-select");
      if (spec.required !== false) control.required = true;
      for (const optionData of spec.options || []) control.add(new Option(optionData.label, optionData.value));
    } else {
      control.type = spec.type || "text";
      control.autocomplete = spec.type === "text" && spec.name === "name" ? "name" : "off";
      control.required = spec.required !== false;
      if (spec.min !== undefined) control.min = spec.min;
      if (spec.max !== undefined) control.max = spec.max;
      if (spec.step !== undefined) control.step = spec.step;
      if (spec.placeholder) control.placeholder = spec.placeholder;
    }
    if (spec.value !== undefined) control.value = spec.value;
    wrapper.append(label, control);
    return wrapper;
  }

  function openCrudDialog(config) {
    state.dialog = config;
    $("#crud-dialog-title").textContent = config.title;
    $("#save-crud-button").textContent = config.saveLabel || "Save";
    $("#crud-form-error").hidden = true;
    const fields = config.fields.map(makeField);
    $("#crud-form-fields").replaceChildren(...fields);
    $("#crud-dialog").showModal();
    requestAnimationFrame(() => $("#crud-form-fields input, #crud-form-fields select")?.focus());
  }

  async function editSkill(skillId) {
    try {
      const skill = await api.getSkill(skillId);
      openCrudDialog({ title: "Edit Skill", fields: [{ name: "name", label: "Skill name", value: skill.name, placeholder: "Enter skill name" }],
        save: async (values) => api.updateSkill(skillId, values), after: loadSkills, feedback: "#skill-feedback", success: "Skill updated." });
    } catch (error) { feedback("#skill-feedback", error.message, true); }
  }

  async function editProject(projectId) {
    try {
      const project = await api.getProject(projectId);
      openCrudDialog({ title: "Edit Project", fields: [{ name: "name", label: "Project name", value: project.name, placeholder: "Enter project name" }],
        save: async (values) => api.updateProject(projectId, values), after: loadProjects, feedback: "#project-feedback", success: "Project updated." });
    } catch (error) { feedback("#project-feedback", error.message, true); }
  }

  async function loadEmployeeSkills(employeeId) {
    const body = $("#employee-skill-body");
    if (!employeeId) {
      tableMessage(body, 3, "Select an employee to view skills.");
      $("#add-employee-skill-button").disabled = true;
      return;
    }
    $("#add-employee-skill-button").disabled = false;
    tableMessage(body, 3, "Loading skills...");
    try {
      const records = await api.getEmployeeSkills(employeeId);
      if (!records.length) tableMessage(body, 3, "No skills assigned to this employee.");
      else {
        body.replaceChildren();
        for (const record of records) appendRow(body, [textCell(record.skillName), textCell(record.proficiency),
          actionCell([actionButton("Edit", "edit-employee-skill", record.skillId), actionButton("Delete", "delete-employee-skill", record.skillId, true)])]);
      }
      feedback("#employee-skill-feedback");
    } catch (error) {
      tableMessage(body, 3, "Employee skills could not be loaded.");
      feedback("#employee-skill-feedback", error.message, true);
    }
  }

  async function loadRequirements(projectId) {
    const body = $("#requirement-table-body");
    if (!projectId) { tableMessage(body, 3, "Select a project to view requirements."); $("#add-requirement-button").disabled = true; return; }
    $("#add-requirement-button").disabled = false;
    tableMessage(body, 3, "Loading requirements...");
    try {
      const records = await api.getProjectRequirements(projectId);
      if (!records.length) tableMessage(body, 3, "No skill requirements for this project.");
      else {
        body.replaceChildren();
        for (const record of records) appendRow(body, [textCell(record.skillName), textCell(record.requiredProficiency),
          actionCell([actionButton("Edit", "edit-requirement", record.skillId), actionButton("Delete", "delete-requirement", record.skillId, true)])]);
      }
      feedback("#requirement-feedback");
    } catch (error) { tableMessage(body, 3, "Requirements could not be loaded."); feedback("#requirement-feedback", error.message, true); }
  }

  async function loadRecommendations(projectId) {
    const list = $("#recommendation-list");
    if (!projectId) { list.replaceChildren(); list.append(textNode("Select a project to see recommendations.")); return; }
    list.replaceChildren(textNode("Loading recommendations..."));
    feedback("#recommendation-feedback");
    try {
      const results = await api.getProjectRecommendations(projectId);
      if (!results.length) { list.replaceChildren(textNode("No suitable employees available for this project.")); return; }
      list.replaceChildren();
      results.forEach((recommendation, index) => {
        const card = document.createElement("article");
        card.className = "recommendation-card";
        const heading = document.createElement("div");
        heading.className = "recommendation-heading";
        const person = document.createElement("div");
        const title = document.createElement("h3");
        title.textContent = recommendation.employeeName;
        const identifier = document.createElement("p");
        identifier.textContent = `Employee #${recommendation.employeeId} · Rank ${index + 1}`;
        person.append(title, identifier);
        const capacity = document.createElement("strong");
        capacity.className = "capacity-value";
        capacity.textContent = `${recommendation.availableCapacity}% available`;
        heading.append(person, capacity);
        const stats = document.createElement("div");
        stats.className = "recommendation-stats";
        stats.textContent = `${recommendation.matchedSkillCount} matched · Proficiency score ${recommendation.proficiencyScore}`;
        const skills = document.createElement("div");
        skills.className = "recommendation-skills";
        const matched = document.createElement("p");
        matched.innerHTML = "";
        const matchedTitle = document.createElement("strong");
        matchedTitle.textContent = "Matched";
        matched.append(matchedTitle, document.createTextNode(recommendation.matchedSkills.length
          ? `: ${recommendation.matchedSkills.map((skill) => `${skill.skillName} (${skill.proficiency})`).join(", ")}` : ": None"));
        const missing = document.createElement("p");
        const missingTitle = document.createElement("strong");
        missingTitle.textContent = "Missing";
        missing.append(missingTitle, document.createTextNode(recommendation.missingSkills.length
          ? `: ${recommendation.missingSkills.join(", ")}` : ": None"));
        skills.append(matched, missing);
        card.append(heading, stats, skills);
        list.append(card);
      });
    } catch (error) {
      list.replaceChildren(textNode("Recommendations could not be loaded."));
      feedback("#recommendation-feedback", error.message, true);
    }
  }

  function textNode(text) {
    const element = document.createElement("p");
    element.className = "table-message";
    element.textContent = text;
    return element;
  }

  async function loadProjectAllocations(projectId) {
    const body = $("#allocation-table-body");
    if (!projectId) { tableMessage(body, 5, "Select a project to view allocations."); return; }
    tableMessage(body, 5, "Loading allocations...");
    try {
      const items = await api.getProjectAllocations(projectId);
      if (!items.length) tableMessage(body, 5, "No allocations found.");
      else {
        body.replaceChildren();
        for (const item of items) appendRow(body, [textCell(item.id), textCell(item.employeeName), textCell(`${item.allocationPercentage}%`), textCell(item.allocationType),
          actionCell([actionButton("Edit", "edit-allocation", item.id), actionButton("Delete", "delete-allocation", item.id, true)])]);
      }
    } catch (error) { tableMessage(body, 5, "Allocations could not be loaded."); feedback("#allocation-feedback", error.message, true); }
  }

  async function loadEmployeeAllocations(employeeId) {
    const body = $("#employee-allocation-table-body");
    if (!employeeId) { tableMessage(body, 5, "Select an employee to view allocations."); return; }
    tableMessage(body, 5, "Loading allocations...");
    try {
      const items = await api.getEmployeeAllocations(employeeId);
      if (!items.length) tableMessage(body, 5, "No allocations found.");
      else {
        body.replaceChildren();
        for (const item of items) appendRow(body, [textCell(item.id), textCell(item.projectName), textCell(`${item.allocationPercentage}%`), textCell(item.allocationType),
          actionCell([actionButton("Edit", "edit-allocation", item.id), actionButton("Delete", "delete-allocation", item.id, true)])]);
      }
    } catch (error) { tableMessage(body, 5, "Allocations could not be loaded."); feedback("#allocation-feedback", error.message, true); }
  }

  function renderCoverage(items) {
    const body = $("#coverage-table-body");
    if (!items.length) { tableMessage(body, 6, "No coverage data available."); return; }
    body.replaceChildren();
    for (const item of items) {
      const status = document.createElement("td");
      const badge = document.createElement("span");
      badge.className = `coverage-status ${item.status === "GAP" ? "is-gap" : "is-covered"}`;
      badge.textContent = item.status;
      status.append(badge);
      appendRow(body, [textCell(item.skillName), textCell(item.requiredProficiency), textCell(item.employeesWithSkill),
        textCell(item.employeesMeetingRequirement), textCell(item.highestProficiency), status]);
    }
  }

  function renderChart(canvasSelector, emptySelector, chartName, config, emptyMessage) {
    const canvas = $(canvasSelector);
    const empty = $(emptySelector);
    if (state.charts[chartName]) { state.charts[chartName].destroy(); state.charts[chartName] = null; }
    if (!window.Chart || !config) {
      canvas.hidden = true;
      empty.hidden = false;
      empty.textContent = window.Chart ? emptyMessage : "Chart.js is unavailable.";
      return;
    }
    empty.hidden = true;
    canvas.hidden = false;
    state.charts[chartName] = new window.Chart(canvas, config);
  }

  async function loadDashboard() {
    feedback("#dashboard-feedback");
    const countElements = Object.fromEntries(["employees", "skills", "projects", "allocations"].map((key) => [key, $(`[data-summary="${key}"]`)]));
    Object.values(countElements).forEach((element) => { element.textContent = "…"; });
    const [employeeResult, skillResult, projectResult] = await Promise.allSettled([api.getEmployees(), api.getSkills(), api.getProjects()]);
    const employees = employeeResult.status === "fulfilled" ? employeeResult.value : null;
    const skills = skillResult.status === "fulfilled" ? skillResult.value : null;
    const projects = projectResult.status === "fulfilled" ? projectResult.value : null;
    if (employees) { state.employees = employees; countElements.employees.textContent = employees.length; }
    else countElements.employees.textContent = "N/A";
    if (skills) { state.skills = skills; countElements.skills.textContent = skills.length; }
    else countElements.skills.textContent = "N/A";
    if (projects) { state.projects = projects; countElements.projects.textContent = projects.length; }
    else countElements.projects.textContent = "N/A";
    refreshSelectors();

    let allocationItems = null;
    if (employees) {
      const allocationsByEmployee = await Promise.allSettled(employees.map((employee) => api.getEmployeeAllocations(employee.id)));
      if (allocationsByEmployee.every((result) => result.status === "fulfilled")) {
        allocationItems = allocationsByEmployee.flatMap((result) => result.value);
        countElements.allocations.textContent = allocationItems.length;
        if (employees.length) {
          const allocated = employees.map((employee) => allocationsByEmployee
            .flatMap((result) => result.value)
            .filter((item) => item.employeeId === employee.id)
            .reduce((sum, item) => sum + item.allocationPercentage, 0));
          const labels = employees.map((employee) => employee.name);
          renderChart("#allocation-chart", "#allocation-chart-empty", "allocation", {
            type: "bar",
            data: { labels, datasets: [
              { label: "Allocated %", data: allocated, backgroundColor: "#4d8b69", borderRadius: 3 },
              { label: "Available %", data: allocated.map((value) => Math.max(0, 100 - value)), backgroundColor: "#d8ef77", borderRadius: 3 }
            ] },
            options: { indexAxis: "y", responsive: true, maintainAspectRatio: false, scales: { x: { stacked: true, min: 0, max: 100, ticks: { callback: (value) => `${value}%` } }, y: { stacked: true } }, plugins: { legend: { position: "bottom" } } }
          }, "No employees found.");
        } else renderChart("#allocation-chart", "#allocation-chart-empty", "allocation", null, "No employees found.");
      } else {
        countElements.allocations.textContent = "N/A";
        renderChart("#allocation-chart", "#allocation-chart-empty", "allocation", null, "Allocation data unavailable.");
      }
    } else {
      countElements.allocations.textContent = "N/A";
      renderChart("#allocation-chart", "#allocation-chart-empty", "allocation", null, "Employee data unavailable.");
    }

    if (skills?.length) {
      try {
        const coverage = await api.getSkillCoverage(skills.map((skill) => skill.id));
        const records = coverage.skills || [];
        const covered = records.filter((item) => item.status === "COVERED").length;
        const gaps = records.filter((item) => item.status === "GAP").length;
        renderChart("#coverage-chart", "#coverage-chart-empty", "coverage", records.length ? {
          type: "doughnut",
          data: { labels: ["Covered", "GAP"], datasets: [{ data: [covered, gaps], backgroundColor: ["#4d8b69", "#f0a184"], borderWidth: 0 }] },
          options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: "bottom" } }, cutout: "68%" }
        } : null, "No coverage data available.");
      } catch (error) {
        renderChart("#coverage-chart", "#coverage-chart-empty", "coverage", null, error.message || "No coverage data available.");
      }
    } else {
      renderChart("#coverage-chart", "#coverage-chart-empty", "coverage", null, skills ? "No skills found." : "Skill data unavailable.");
    }
    if (employeeResult.status === "rejected" || skillResult.status === "rejected" || projectResult.status === "rejected") {
      feedback("#dashboard-feedback", "Some dashboard data could not be loaded.", true);
    }
  }

  async function loadSkillCoverage() {
    const ids = [...$("#coverage-skill-select").selectedOptions].map((option) => Number(option.value)).filter(Number.isFinite);
    if (!ids.length) { feedback("#coverage-feedback", "Select at least one skill.", true); return; }
    tableMessage($("#coverage-table-body"), 6, "Loading coverage...");
    feedback("#coverage-feedback");
    try { renderCoverage((await api.getSkillCoverage(ids)).skills || []); }
    catch (error) { tableMessage($("#coverage-table-body"), 6, "Coverage could not be loaded."); feedback("#coverage-feedback", error.message, true); }
  }

  async function loadProjectCoverage(projectId) {
    if (!projectId) { feedback("#coverage-feedback", "Select a project.", true); return; }
    tableMessage($("#coverage-table-body"), 6, "Loading coverage...");
    feedback("#coverage-feedback");
    try { renderCoverage((await api.getProjectCoverage(projectId)).skills || []); }
    catch (error) { tableMessage($("#coverage-table-body"), 6, "Coverage could not be loaded."); feedback("#coverage-feedback", error.message, true); }
  }

  async function showView(viewName) {
    const selectedButton = navButtons.find((button) => button.dataset.view === viewName);
    if (!selectedButton) return;
    state.currentView = viewName;
    navButtons.forEach((button) => {
      const active = button === selectedButton;
      button.classList.toggle("is-active", active);
      if (active) button.setAttribute("aria-current", "page"); else button.removeAttribute("aria-current");
    });
    views.forEach((view) => { const active = view.id === `view-${viewName}`; view.classList.toggle("is-visible", active); view.hidden = !active; });
    $("#page-title").textContent = selectedButton.dataset.title;
    $("#page-description").textContent = selectedButton.dataset.description;
    $("#breadcrumb-title").textContent = selectedButton.dataset.title;
    document.title = `SkillMatrix | ${selectedButton.dataset.title}`;
    try {
      if (viewName === "dashboard") await loadDashboard();
      if (viewName === "employees") { await loadEmployees(); await loadEmployeeSkills($("#employee-skill-employee").value); }
      if (viewName === "skills") await loadSkills();
      if (viewName === "projects") { await Promise.all([loadProjects(), loadSkills()]); await loadRequirements($("#requirement-project").value); }
      if (viewName === "recommendations" || viewName === "allocations" || viewName === "coverage") await loadReferenceData();
      if (viewName === "allocations") {
        await Promise.all([loadProjectAllocations($("#allocation-list-project").value), loadEmployeeAllocations($("#allocation-list-employee").value)]);
      }
    } catch {
      // Section loaders render their own request errors.
    }
  }

  function openEmployeeAddDialog() {
    state.employeeEditingId = null;
    $("#employee-form").reset();
    $("#employee-name").disabled = false;
    $("#employee-dialog-title").textContent = "Add Employee";
    $("#save-employee-button").textContent = "Save Employee";
    $("#employee-form-error").hidden = true;
    $("#employee-dialog").showModal();
    requestAnimationFrame(() => $("#employee-name").focus());
  }

  async function openEmployeeEditDialog(employeeId) {
    state.employeeEditingId = employeeId;
    $("#employee-form").reset();
    $("#employee-name").disabled = true;
    $("#employee-dialog-title").textContent = "Edit Employee";
    $("#save-employee-button").textContent = "Save Changes";
    $("#employee-dialog").showModal();
    try {
      const employee = await api.getEmployee(employeeId);
      $("#employee-name").value = employee.name;
      $("#employee-name").disabled = false;
      $("#employee-name").focus();
    } catch (error) { $("#employee-form-error").textContent = error.message; $("#employee-form-error").hidden = false; }
  }

  function openCrudDialog(config) {
    state.dialog = config;
    $("#crud-dialog-title").textContent = config.title;
    $("#save-crud-button").textContent = config.saveLabel || "Save";
    $("#crud-form-error").hidden = true;
    $("#crud-form-fields").replaceChildren(...config.fields.map(makeField));
    $("#crud-dialog").showModal();
    requestAnimationFrame(() => $("#crud-form-fields input, #crud-form-fields select")?.focus());
  }

  async function editSkill(skillId) {
    try {
      const skill = await api.getSkill(skillId);
      openCrudDialog({ title: "Edit Skill", fields: [{ name: "name", label: "Skill name", value: skill.name, placeholder: "Enter skill name" }],
        save: (values) => api.updateSkill(skillId, values), after: loadSkills, feedback: "#skill-feedback", success: "Skill updated." });
    } catch (error) { feedback("#skill-feedback", error.message, true); }
  }

  async function editProject(projectId) {
    try {
      const project = await api.getProject(projectId);
      openCrudDialog({ title: "Edit Project", fields: [{ name: "name", label: "Project name", value: project.name, placeholder: "Enter project name" }],
        save: (values) => api.updateProject(projectId, values), after: loadProjects, feedback: "#project-feedback", success: "Project updated." });
    } catch (error) { feedback("#project-feedback", error.message, true); }
  }

  async function loadEmployeeSkills(employeeId) {
    const body = $("#employee-skill-body");
    if (!employeeId) { tableMessage(body, 3, "Select an employee to view skills."); $("#add-employee-skill-button").disabled = true; return; }
    $("#add-employee-skill-button").disabled = false;
    tableMessage(body, 3, "Loading skills...");
    try {
      const records = await api.getEmployeeSkills(employeeId);
      if (!records.length) tableMessage(body, 3, "No skills assigned to this employee.");
      else {
        body.replaceChildren();
        records.forEach((record) => appendRow(body, [textCell(record.skillName), textCell(record.proficiency),
          actionCell([actionButton("Edit", "edit-employee-skill", record.skillId), actionButton("Delete", "delete-employee-skill", record.skillId, true)])]));
      }
      feedback("#employee-skill-feedback");
    } catch (error) { tableMessage(body, 3, "Employee skills could not be loaded."); feedback("#employee-skill-feedback", error.message, true); }
  }

  async function loadRequirements(projectId) {
    const body = $("#requirement-table-body");
    if (!projectId) { tableMessage(body, 3, "Select a project to view requirements."); $("#add-requirement-button").disabled = true; return; }
    $("#add-requirement-button").disabled = false;
    tableMessage(body, 3, "Loading requirements...");
    try {
      const records = await api.getProjectRequirements(projectId);
      if (!records.length) tableMessage(body, 3, "No skill requirements for this project.");
      else {
        body.replaceChildren();
        records.forEach((record) => appendRow(body, [textCell(record.skillName), textCell(record.requiredProficiency),
          actionCell([actionButton("Edit", "edit-requirement", record.skillId), actionButton("Delete", "delete-requirement", record.skillId, true)])]));
      }
      feedback("#requirement-feedback");
    } catch (error) { tableMessage(body, 3, "Requirements could not be loaded."); feedback("#requirement-feedback", error.message, true); }
  }

  async function loadRecommendations(projectId) {
    const list = $("#recommendation-list");
    if (!projectId) { list.replaceChildren(textNode("Select a project to see recommendations.")); return; }
    list.replaceChildren(textNode("Loading recommendations..."));
    feedback("#recommendation-feedback");
    try {
      const results = await api.getProjectRecommendations(projectId);
      if (!results.length) { list.replaceChildren(textNode("No suitable employees available for this project.")); return; }
      list.replaceChildren();
      results.forEach((item, index) => {
        const card = document.createElement("article"); card.className = "recommendation-card";
        const heading = document.createElement("div"); heading.className = "recommendation-heading";
        const person = document.createElement("div");
        const title = document.createElement("h3"); title.textContent = item.employeeName;
        const identifier = document.createElement("p"); identifier.textContent = `Employee #${item.employeeId} · Rank ${index + 1}`;
        person.append(title, identifier);
        const capacity = document.createElement("strong"); capacity.className = "capacity-value"; capacity.textContent = `${item.availableCapacity}% available`;
        heading.append(person, capacity);
        const stats = document.createElement("div"); stats.className = "recommendation-stats";
        stats.textContent = `${item.matchedSkillCount} matched · Proficiency score ${item.proficiencyScore}`;
        const matched = document.createElement("p"); const matchedTitle = document.createElement("strong"); matchedTitle.textContent = "Matched";
        matched.append(matchedTitle, document.createTextNode(item.matchedSkills.length ? `: ${item.matchedSkills.map((skill) => `${skill.skillName} (${skill.proficiency})`).join(", ")}` : ": None"));
        const missing = document.createElement("p"); const missingTitle = document.createElement("strong"); missingTitle.textContent = "Missing";
        missing.append(missingTitle, document.createTextNode(item.missingSkills.length ? `: ${item.missingSkills.join(", ")}` : ": None"));
        const skills = document.createElement("div"); skills.className = "recommendation-skills"; skills.append(matched, missing);
        card.append(heading, stats, skills); list.append(card);
      });
    } catch (error) { list.replaceChildren(textNode("Recommendations could not be loaded.")); feedback("#recommendation-feedback", error.message, true); }
  }

  function textNode(text) { const element = document.createElement("p"); element.className = "table-message"; element.textContent = text; return element; }

  async function loadProjectAllocations(projectId) {
    const body = $("#allocation-table-body");
    if (!projectId) { tableMessage(body, 5, "Select a project to view allocations."); return; }
    tableMessage(body, 5, "Loading allocations...");
    try {
      const items = await api.getProjectAllocations(projectId);
      if (!items.length) tableMessage(body, 5, "No allocations found.");
      else { body.replaceChildren(); items.forEach((item) => appendRow(body, [textCell(item.id), textCell(item.employeeName), textCell(`${item.allocationPercentage}%`), textCell(item.allocationType), actionCell([actionButton("Edit", "edit-allocation", item.id), actionButton("Delete", "delete-allocation", item.id, true)])])); }
    } catch (error) { tableMessage(body, 5, "Allocations could not be loaded."); feedback("#allocation-feedback", error.message, true); }
  }

  async function loadEmployeeAllocations(employeeId) {
    const body = $("#employee-allocation-table-body");
    if (!employeeId) { tableMessage(body, 5, "Select an employee to view allocations."); return; }
    tableMessage(body, 5, "Loading allocations...");
    try {
      const items = await api.getEmployeeAllocations(employeeId);
      if (!items.length) tableMessage(body, 5, "No allocations found.");
      else { body.replaceChildren(); items.forEach((item) => appendRow(body, [textCell(item.id), textCell(item.projectName), textCell(`${item.allocationPercentage}%`), textCell(item.allocationType), actionCell([actionButton("Edit", "edit-allocation", item.id), actionButton("Delete", "delete-allocation", item.id, true)])])); }
    } catch (error) { tableMessage(body, 5, "Allocations could not be loaded."); feedback("#allocation-feedback", error.message, true); }
  }

  function renderCoverage(items) {
    const body = $("#coverage-table-body");
    if (!items.length) { tableMessage(body, 6, "No coverage data available."); return; }
    body.replaceChildren();
    items.forEach((item) => {
      const status = document.createElement("td");
      const badge = document.createElement("span");
      badge.className = `coverage-status ${item.status === "GAP" ? "is-gap" : "is-covered"}`;
      badge.textContent = item.status; status.append(badge);
      appendRow(body, [textCell(item.skillName), textCell(item.requiredProficiency), textCell(item.employeesWithSkill), textCell(item.employeesMeetingRequirement), textCell(item.highestProficiency), status]);
    });
  }

  function renderChart(canvasSelector, emptySelector, chartName, config, emptyMessage) {
    const canvas = $(canvasSelector); const empty = $(emptySelector);
    if (state.charts[chartName]) { state.charts[chartName].destroy(); state.charts[chartName] = null; }
    if (!window.Chart || !config) { canvas.hidden = true; empty.hidden = false; empty.textContent = window.Chart ? emptyMessage : "Chart.js is unavailable."; return; }
    empty.hidden = true; canvas.hidden = false; state.charts[chartName] = new window.Chart(canvas, config);
  }

  async function loadDashboard() {
    feedback("#dashboard-feedback");
    const counts = Object.fromEntries(["employees", "skills", "projects", "allocations"].map((key) => [key, $(`[data-summary="${key}"]`)]));
    Object.values(counts).forEach((element) => { element.textContent = "…"; });
    const [employeeResult, skillResult, projectResult] = await Promise.allSettled([api.getEmployees(), api.getSkills(), api.getProjects()]);
    const employees = employeeResult.status === "fulfilled" ? employeeResult.value : null;
    const skills = skillResult.status === "fulfilled" ? skillResult.value : null;
    const projects = projectResult.status === "fulfilled" ? projectResult.value : null;
    if (employees) { state.employees = employees; counts.employees.textContent = employees.length; } else counts.employees.textContent = "N/A";
    if (skills) { state.skills = skills; counts.skills.textContent = skills.length; } else counts.skills.textContent = "N/A";
    if (projects) { state.projects = projects; counts.projects.textContent = projects.length; } else counts.projects.textContent = "N/A";
    refreshSelectors();

    if (employees) {
      const allocationsByEmployee = await Promise.allSettled(employees.map((employee) => api.getEmployeeAllocations(employee.id)));
      if (allocationsByEmployee.every((result) => result.status === "fulfilled")) {
        const allocations = allocationsByEmployee.flatMap((result) => result.value);
        counts.allocations.textContent = allocations.length;
        const values = employees.map((employee) => allocations.filter((item) => item.employeeId === employee.id).reduce((sum, item) => sum + item.allocationPercentage, 0));
        const config = employees.length ? { type: "bar", data: { labels: employees.map((employee) => employee.name), datasets: [
          { label: "Allocated %", data: values, backgroundColor: "#4d8b69", borderRadius: 3 },
          { label: "Available %", data: values.map((value) => Math.max(0, 100 - value)), backgroundColor: "#d8ef77", borderRadius: 3 }
        ] }, options: { indexAxis: "y", responsive: true, maintainAspectRatio: false, scales: { x: { stacked: true, min: 0, max: 100, ticks: { callback: (value) => `${value}%` } }, y: { stacked: true } }, plugins: { legend: { position: "bottom" } } } } : null;
        renderChart("#allocation-chart", "#allocation-chart-empty", "allocation", config, "No employees found.");
      } else { counts.allocations.textContent = "N/A"; renderChart("#allocation-chart", "#allocation-chart-empty", "allocation", null, "Allocation data unavailable."); }
    } else { counts.allocations.textContent = "N/A"; renderChart("#allocation-chart", "#allocation-chart-empty", "allocation", null, "Employee data unavailable."); }

    if (skills?.length) {
      try {
        const records = (await api.getSkillCoverage(skills.map((skill) => skill.id))).skills || [];
        const covered = records.filter((item) => item.status === "COVERED").length;
        const gaps = records.filter((item) => item.status === "GAP").length;
        const config = records.length ? { type: "doughnut", data: { labels: ["Covered", "GAP"], datasets: [{ data: [covered, gaps], backgroundColor: ["#4d8b69", "#f0a184"], borderWidth: 0 }] }, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: "bottom" } }, cutout: "68%" } } : null;
        renderChart("#coverage-chart", "#coverage-chart-empty", "coverage", config, "No coverage data available.");
      } catch (error) { renderChart("#coverage-chart", "#coverage-chart-empty", "coverage", null, error.message); }
    } else renderChart("#coverage-chart", "#coverage-chart-empty", "coverage", null, skills ? "No skills found." : "Skill data unavailable.");
    if (employeeResult.status === "rejected" || skillResult.status === "rejected" || projectResult.status === "rejected") feedback("#dashboard-feedback", "Some dashboard data could not be loaded.", true);
  }

  async function loadSkillCoverage() {
    const ids = [...$("#coverage-skill-select").selectedOptions].map((option) => Number(option.value)).filter(Number.isFinite);
    if (!ids.length) { feedback("#coverage-feedback", "Select at least one skill.", true); return; }
    tableMessage($("#coverage-table-body"), 6, "Loading coverage..."); feedback("#coverage-feedback");
    try { renderCoverage((await api.getSkillCoverage(ids)).skills || []); }
    catch (error) { tableMessage($("#coverage-table-body"), 6, "Coverage could not be loaded."); feedback("#coverage-feedback", error.message, true); }
  }

  async function loadProjectCoverage(projectId) {
    if (!projectId) { feedback("#coverage-feedback", "Select a project.", true); return; }
    tableMessage($("#coverage-table-body"), 6, "Loading coverage..."); feedback("#coverage-feedback");
    try { renderCoverage((await api.getProjectCoverage(projectId)).skills || []); }
    catch (error) { tableMessage($("#coverage-table-body"), 6, "Coverage could not be loaded."); feedback("#coverage-feedback", error.message, true); }
  }

  async function showView(viewName) {
    const selectedButton = navButtons.find((button) => button.dataset.view === viewName);
    if (!selectedButton) return;
    state.currentView = viewName;
    navButtons.forEach((button) => { const active = button === selectedButton; button.classList.toggle("is-active", active); if (active) button.setAttribute("aria-current", "page"); else button.removeAttribute("aria-current"); });
    views.forEach((view) => { const active = view.id === `view-${viewName}`; view.classList.toggle("is-visible", active); view.hidden = !active; });
    $("#page-title").textContent = selectedButton.dataset.title;
    $("#page-description").textContent = selectedButton.dataset.description;
    $("#breadcrumb-title").textContent = selectedButton.dataset.title;
    document.title = `SkillMatrix | ${selectedButton.dataset.title}`;
    try {
      if (viewName === "dashboard") await loadDashboard();
      if (viewName === "employees") { await loadEmployees(); await loadEmployeeSkills($("#employee-skill-employee").value); }
      if (viewName === "skills") await loadSkills();
      if (viewName === "projects") { await Promise.all([loadProjects(), loadSkills()]); await loadRequirements($("#requirement-project").value); }
      if (["recommendations", "allocations", "coverage"].includes(viewName)) await loadReferenceData();
      if (viewName === "allocations") await Promise.all([loadProjectAllocations($("#allocation-list-project").value), loadEmployeeAllocations($("#allocation-list-employee").value)]);
    } catch { /* Loaders display their own errors. */ }
  }

  function setFormError(selector, message) { const error = $(selector); error.textContent = message || ""; error.hidden = !message; }

  function openEmployeeAddDialog() {
    state.employeeEditingId = null; $("#employee-form").reset(); $("#employee-name").disabled = false;
    $("#employee-dialog-title").textContent = "Add Employee"; $("#save-employee-button").textContent = "Save Employee";
    setFormError("#employee-form-error", ""); $("#employee-dialog").showModal(); requestAnimationFrame(() => $("#employee-name").focus());
  }

  async function openEmployeeEditDialog(employeeId) {
    state.employeeEditingId = employeeId; $("#employee-form").reset(); $("#employee-name").disabled = true;
    $("#employee-dialog-title").textContent = "Edit Employee"; $("#save-employee-button").textContent = "Save Changes";
    $("#employee-dialog").showModal();
    try { const employee = await api.getEmployee(employeeId); $("#employee-name").value = employee.name; $("#employee-name").disabled = false; $("#employee-name").focus(); }
    catch (error) { setFormError("#employee-form-error", error.message); }
  }

  function makeField(spec) {
    const wrapper = document.createElement("label"); wrapper.className = "form-field dialog-field";
    const label = document.createElement("span"); label.textContent = spec.label;
    const control = spec.type === "select" ? document.createElement("select") : document.createElement("input");
    control.name = spec.name; control.id = `crud-${spec.name}`; control.className = spec.type === "select" ? "control-select" : "control-input";
    if (spec.type === "select") { control.required = spec.required !== false; (spec.options || []).forEach((option) => control.add(new Option(option.label, option.value))); }
    else { control.type = spec.type || "text"; control.required = spec.required !== false; control.autocomplete = spec.name === "name" ? "name" : "off"; if (spec.min !== undefined) control.min = spec.min; if (spec.max !== undefined) control.max = spec.max; if (spec.step !== undefined) control.step = spec.step; if (spec.placeholder) control.placeholder = spec.placeholder; }
    if (spec.value !== undefined) control.value = spec.value;
    wrapper.append(label, control); return wrapper;
  }

  function openCrudDialog(config) {
    state.dialog = config; $("#crud-dialog-title").textContent = config.title; $("#save-crud-button").textContent = config.saveLabel || "Save";
    setFormError("#crud-form-error", ""); $("#crud-form-fields").replaceChildren(...config.fields.map(makeField));
    $("#crud-dialog").showModal(); requestAnimationFrame(() => $("#crud-form-fields input, #crud-form-fields select")?.focus());
  }

  async function editSkill(skillId) {
    try { const skill = await api.getSkill(skillId); openCrudDialog({ title: "Edit Skill", fields: [{ name: "name", label: "Skill name", value: skill.name, placeholder: "Enter skill name" }], save: (values) => api.updateSkill(skillId, values), after: loadSkills, feedback: "#skill-feedback", success: "Skill updated." }); }
    catch (error) { feedback("#skill-feedback", error.message, true); }
  }

  async function editProject(projectId) {
    try { const project = await api.getProject(projectId); openCrudDialog({ title: "Edit Project", fields: [{ name: "name", label: "Project name", value: project.name, placeholder: "Enter project name" }], save: (values) => api.updateProject(projectId, values), after: loadProjects, feedback: "#project-feedback", success: "Project updated." }); }
    catch (error) { feedback("#project-feedback", error.message, true); }
  }

  async function addEmployeeSkill() {
    const employeeId = $("#employee-skill-employee").value;
    if (!employeeId) return;
    let assigned = [];
    try { assigned = await api.getEmployeeSkills(employeeId); } catch (error) { feedback("#employee-skill-feedback", error.message, true); return; }
    const availableSkills = state.skills.filter((skill) => !assigned.some((record) => record.skillId === skill.id));
    if (!availableSkills.length) { feedback("#employee-skill-feedback", "No unassigned skills are available.", true); return; }
    openCrudDialog({ title: "Add Employee Skill", fields: [
      { name: "skillId", label: "Skill", type: "select", options: availableSkills.map((skill) => ({ value: skill.id, label: skill.name })) },
      { name: "proficiency", label: "Proficiency", type: "select", options: [1, 2, 3, 4, 5].map((value) => ({ value, label: String(value) })) }
    ], parse: (values) => ({ skillId: Number(values.skillId), proficiency: Number(values.proficiency) }),
    save: (values) => api.addEmployeeSkill(employeeId, values), after: () => loadEmployeeSkills(employeeId), feedback: "#employee-skill-feedback", success: "Employee skill added." });
  }

  async function editEmployeeSkill(skillId) {
    const employeeId = $("#employee-skill-employee").value;
    try {
      const records = await api.getEmployeeSkills(employeeId); const record = records.find((item) => item.skillId === Number(skillId));
      if (!record) throw new Error("Employee skill was not found.");
      openCrudDialog({ title: `Edit ${record.skillName} proficiency`, fields: [{ name: "proficiency", label: "Proficiency", type: "select", value: record.proficiency, options: [1, 2, 3, 4, 5].map((value) => ({ value, label: String(value) })) }], parse: (values) => ({ proficiency: Number(values.proficiency) }),
        save: (values) => api.updateEmployeeSkill(employeeId, skillId, values), after: () => loadEmployeeSkills(employeeId), feedback: "#employee-skill-feedback", success: "Proficiency updated." });
    } catch (error) { feedback("#employee-skill-feedback", error.message, true); }
  }

  async function addRequirement() {
    const projectId = $("#requirement-project").value; if (!projectId) return;
    let assigned = [];
    try { assigned = await api.getProjectRequirements(projectId); } catch (error) { feedback("#requirement-feedback", error.message, true); return; }
    const availableSkills = state.skills.filter((skill) => !assigned.some((record) => record.skillId === skill.id));
    if (!availableSkills.length) { feedback("#requirement-feedback", "No unassigned skills are available.", true); return; }
    openCrudDialog({ title: "Add Required Skill", fields: [
      { name: "skillId", label: "Skill", type: "select", options: availableSkills.map((skill) => ({ value: skill.id, label: skill.name })) },
      { name: "requiredProficiency", label: "Required proficiency", type: "select", options: [1, 2, 3, 4, 5].map((value) => ({ value, label: String(value) })) }
    ], parse: (values) => ({ skillId: Number(values.skillId), requiredProficiency: Number(values.requiredProficiency) }),
    save: (values) => api.addProjectRequirement(projectId, values), after: () => loadRequirements(projectId), feedback: "#requirement-feedback", success: "Requirement added." });
  }

  async function editRequirement(skillId) {
    const projectId = $("#requirement-project").value;
    try {
      const records = await api.getProjectRequirements(projectId); const record = records.find((item) => item.skillId === Number(skillId));
      if (!record) throw new Error("Project requirement was not found.");
      openCrudDialog({ title: `Edit ${record.skillName} requirement`, fields: [{ name: "requiredProficiency", label: "Required proficiency", type: "select", value: record.requiredProficiency, options: [1, 2, 3, 4, 5].map((value) => ({ value, label: String(value) })) }], parse: (values) => ({ requiredProficiency: Number(values.requiredProficiency) }),
        save: (values) => api.updateProjectRequirement(projectId, skillId, values), after: () => loadRequirements(projectId), feedback: "#requirement-feedback", success: "Requirement updated." });
    } catch (error) { feedback("#requirement-feedback", error.message, true); }
  }

  async function editAllocation(allocationId) {
    const projectId = $("#allocation-list-project").value;
    const employeeId = $("#allocation-list-employee").value;
    const lookups = await Promise.allSettled([
      projectId ? api.getProjectAllocations(projectId) : Promise.resolve([]),
      employeeId ? api.getEmployeeAllocations(employeeId) : Promise.resolve([])
    ]);
    const allocation = lookups.flatMap((result) => result.status === "fulfilled" ? result.value : [])
      .find((item) => item.id === Number(allocationId));
    if (!allocation) { feedback("#allocation-feedback", "Allocation was not found.", true); return; }
    openCrudDialog({ title: `Edit allocation for ${allocation.employeeName}`, fields: [
      { name: "allocationPercentage", label: "Allocation percentage", type: "number", min: 1, max: 100, step: 1, value: allocation.allocationPercentage },
      { name: "allocationType", label: "Allocation type", type: "select", value: allocation.allocationType, options: [{ value: "PARTIAL", label: "PARTIAL" }, { value: "FULL", label: "FULL" }] }
    ], parse: (values) => ({ allocationPercentage: Number(values.allocationPercentage), allocationType: values.allocationType }),
    validate: validateAllocationValues, save: (values) => api.updateAllocation(allocationId, values), after: refreshAllocations, feedback: "#allocation-feedback", success: "Allocation updated." });
  }

  function validateAllocationValues(values) {
    if (!Number.isInteger(values.allocationPercentage) || values.allocationPercentage < 1 || values.allocationPercentage > 100) return "Allocation must be a whole number between 1 and 100.";
    if (values.allocationType === "FULL" && values.allocationPercentage !== 100) return "FULL allocation must be exactly 100%.";
    if (values.allocationType === "PARTIAL" && values.allocationPercentage >= 100) return "PARTIAL allocation must be below 100%.";
    return "";
  }

  async function refreshAllocations(selection = {}) {
    if (selection.projectId) $("#allocation-list-project").value = String(selection.projectId);
    if (selection.employeeId) $("#allocation-list-employee").value = String(selection.employeeId);
    await Promise.all([loadProjectAllocations($("#allocation-list-project").value), loadEmployeeAllocations($("#allocation-list-employee").value)]);
    if (state.currentView === "dashboard") await loadDashboard();
  }

  async function deleteAndRefresh({ action, id, confirmMessage, after, feedbackSelector, success }) {
    if (!window.confirm(confirmMessage)) return;
    try { await action(id); await after(); feedback(feedbackSelector, success); if (state.currentView === "dashboard") await loadDashboard(); }
    catch (error) { feedback(feedbackSelector, error.message, true); }
  }

  function registerTableActions(selector, handler) {
    $(selector).addEventListener("click", (event) => {
      const button = event.target.closest("[data-action]");
      if (button) handler(button.dataset.action, button.dataset.id);
    });
  }

  function submitCrudForm() {
    $("#crud-form").addEventListener("submit", async (event) => {
      event.preventDefault();
      if (!state.dialog) return;
      const raw = Object.fromEntries(new FormData(event.currentTarget).entries());
      const values = state.dialog.parse ? state.dialog.parse(raw) : Object.fromEntries(Object.entries(raw).map(([key, value]) => [key, value.trim()]));
      const formError = state.dialog.validate?.(values) || (Object.values(values).some((value) => value === "") ? "Complete the required fields." : "");
      if (formError) { setFormError("#crud-form-error", formError); return; }
      const saveButton = $("#save-crud-button"); saveButton.disabled = true;
      try {
        await state.dialog.save(values);
        const { after, feedback: feedbackSelector, success } = state.dialog;
        $("#crud-dialog").close(); await after(); feedback(feedbackSelector, success);
        if (state.currentView === "dashboard") await loadDashboard();
      } catch (error) { setFormError("#crud-form-error", error.message || "The request could not be completed."); }
      finally { saveButton.disabled = false; }
    });
  }

  function registerEvents() {
    $(".primary-nav").addEventListener("click", (event) => { const button = event.target.closest("[data-view]"); if (button) showView(button.dataset.view); });
    $$ ("[data-go-view]").forEach((button) => button.addEventListener("click", () => showView(button.dataset.goView)));
    $("#refresh-view-button").addEventListener("click", () => showView(state.currentView));

    $("#add-employee-button").addEventListener("click", openEmployeeAddDialog);
    $("#close-employee-dialog").addEventListener("click", () => $("#employee-dialog").close());
    $("#cancel-employee-button").addEventListener("click", () => $("#employee-dialog").close());
    $("#employee-dialog").addEventListener("close", () => { $("#employee-form").reset(); $("#employee-name").disabled = false; setFormError("#employee-form-error", ""); state.employeeEditingId = null; });
    $("#employee-name").addEventListener("input", () => setFormError("#employee-form-error", ""));
    $("#employee-form").addEventListener("submit", async (event) => {
      event.preventDefault(); const name = $("#employee-name").value.trim();
      if (!name) { setFormError("#employee-form-error", "Name cannot be empty."); $("#employee-name").focus(); return; }
      const save = $("#save-employee-button"); save.disabled = true;
      try {
        if (state.employeeEditingId === null) await api.createEmployee({ name }); else await api.updateEmployee(state.employeeEditingId, { name });
        const success = state.employeeEditingId === null ? "Employee added." : "Employee updated.";
        $("#employee-dialog").close(); await loadEmployees(); feedback("#employee-feedback", success);
        if (state.currentView === "dashboard") await loadDashboard();
      } catch (error) { setFormError("#employee-form-error", error.message); }
      finally { save.disabled = false; }
    });
    registerTableActions("#employee-table-body", async (action, id) => {
      if (action === "edit-employee") await openEmployeeEditDialog(id);
      if (action === "delete-employee") await deleteAndRefresh({ action: api.deleteEmployee, id, confirmMessage: "Delete this employee? This action cannot be undone.", after: loadEmployees, feedbackSelector: "#employee-feedback", success: "Employee deleted." });
    });

    $("#add-skill-button").addEventListener("click", () => openCrudDialog({ title: "Add Skill", fields: [{ name: "name", label: "Skill name", placeholder: "Enter skill name" }], save: api.createSkill, after: loadSkills, feedback: "#skill-feedback", success: "Skill added." }));
    registerTableActions("#skill-table-body", async (action, id) => {
      if (action === "edit-skill") await editSkill(id);
      if (action === "delete-skill") await deleteAndRefresh({ action: api.deleteSkill, id, confirmMessage: "Delete this skill?", after: loadSkills, feedbackSelector: "#skill-feedback", success: "Skill deleted." });
    });

    $("#add-project-button").addEventListener("click", () => openCrudDialog({ title: "Add Project", fields: [{ name: "name", label: "Project name", placeholder: "Enter project name" }], save: api.createProject, after: loadProjects, feedback: "#project-feedback", success: "Project added." }));
    registerTableActions("#project-table-body", async (action, id) => {
      if (action === "edit-project") await editProject(id);
      if (action === "delete-project") await deleteAndRefresh({ action: api.deleteProject, id, confirmMessage: "Delete this project?", after: loadProjects, feedbackSelector: "#project-feedback", success: "Project deleted." });
    });

    $("#employee-skill-employee").addEventListener("change", (event) => loadEmployeeSkills(event.target.value));
    $("#add-employee-skill-button").addEventListener("click", addEmployeeSkill);
    registerTableActions("#employee-skill-body", async (action, id) => {
      const employeeId = $("#employee-skill-employee").value;
      if (action === "edit-employee-skill") await editEmployeeSkill(id);
      if (action === "delete-employee-skill") await deleteAndRefresh({ action: (skillId) => api.deleteEmployeeSkill(employeeId, skillId), id, confirmMessage: "Remove this skill from the employee?", after: () => loadEmployeeSkills(employeeId), feedbackSelector: "#employee-skill-feedback", success: "Employee skill removed." });
    });

    $("#requirement-project").addEventListener("change", (event) => loadRequirements(event.target.value));
    $("#add-requirement-button").addEventListener("click", addRequirement);
    registerTableActions("#requirement-table-body", async (action, id) => {
      const projectId = $("#requirement-project").value;
      if (action === "edit-requirement") await editRequirement(id);
      if (action === "delete-requirement") await deleteAndRefresh({ action: (skillId) => api.deleteProjectRequirement(projectId, skillId), id, confirmMessage: "Remove this project requirement?", after: () => loadRequirements(projectId), feedbackSelector: "#requirement-feedback", success: "Requirement removed." });
    });

    $("#recommendation-project").addEventListener("change", (event) => { $("#load-recommendations-button").disabled = !event.target.value; });
    $("#load-recommendations-button").addEventListener("click", () => loadRecommendations($("#recommendation-project").value));

    $("#allocation-form").addEventListener("submit", async (event) => {
      event.preventDefault();
      const form = event.currentTarget;
      const projectId = $("#allocation-project").value;
      const payload = { employeeId: Number($("#allocation-employee").value), allocationPercentage: Number($("#allocation-percentage").value), allocationType: $("#allocation-type").value };
      const error = validateAllocationValues({ allocationPercentage: payload.allocationPercentage, allocationType: payload.allocationType });
      if (!projectId || !payload.employeeId) { feedback("#allocation-feedback", "Select both a project and an employee.", true); return; }
      if (error) { feedback("#allocation-feedback", error, true); return; }
      const submit = $("#save-allocation-button"); submit.disabled = true;
      try { await api.createAllocation(projectId, payload); form.reset(); await refreshAllocations({ projectId, employeeId: payload.employeeId }); feedback("#allocation-feedback", "Allocation created."); }
      catch (requestError) { feedback("#allocation-feedback", requestError.message, true); }
      finally { submit.disabled = false; }
    });
    $("#allocation-list-project").addEventListener("change", (event) => loadProjectAllocations(event.target.value));
    $("#allocation-list-employee").addEventListener("change", (event) => loadEmployeeAllocations(event.target.value));
    registerTableActions("#allocation-table-body", async (action, id) => {
      if (action === "edit-allocation") await editAllocation(id);
      if (action === "delete-allocation") await deleteAndRefresh({ action: api.deleteAllocation, id, confirmMessage: "Delete this allocation?", after: refreshAllocations, feedbackSelector: "#allocation-feedback", success: "Allocation deleted." });
    });
    registerTableActions("#employee-allocation-table-body", async (action, id) => {
      if (action === "edit-allocation") await editAllocation(id);
      if (action === "delete-allocation") await deleteAndRefresh({ action: api.deleteAllocation, id, confirmMessage: "Delete this allocation?", after: refreshAllocations, feedbackSelector: "#allocation-feedback", success: "Allocation deleted." });
    });

    $("#load-skill-coverage-button").addEventListener("click", loadSkillCoverage);
    $("#coverage-project-select").addEventListener("change", (event) => { $("#load-project-coverage-button").disabled = !event.target.value; });
    $("#load-project-coverage-button").addEventListener("click", () => loadProjectCoverage($("#coverage-project-select").value));

    $("#close-crud-dialog").addEventListener("click", () => $("#crud-dialog").close());
    $("#cancel-crud-button").addEventListener("click", () => $("#crud-dialog").close());
    $("#crud-dialog").addEventListener("close", () => { $("#crud-form").reset(); $("#crud-form-fields").replaceChildren(); state.dialog = null; });
    $("#crud-form-fields").addEventListener("input", () => setFormError("#crud-form-error", ""));
    $("#crud-form").addEventListener("submit", async (event) => {
      event.preventDefault(); if (!state.dialog) return;
      const raw = Object.fromEntries(new FormData(event.currentTarget).entries());
      const values = state.dialog.parse ? state.dialog.parse(raw) : Object.fromEntries(Object.entries(raw).map(([key, value]) => [key, value.trim()]));
      const validation = state.dialog.validate?.(values) || (Object.values(values).some((value) => value === "") ? "Complete the required fields." : "");
      if (validation) { setFormError("#crud-form-error", validation); return; }
      const button = $("#save-crud-button"); button.disabled = true;
      try { await state.dialog.save(values); const config = state.dialog; $("#crud-dialog").close(); await config.after(); feedback(config.feedback, config.success); }
      catch (error) { setFormError("#crud-form-error", error.message || "The request could not be completed."); }
      finally { button.disabled = false; }
    });
  }

  function init() {
    registerEvents();
    $("#current-date").textContent = new Intl.DateTimeFormat(undefined, { weekday: "short", month: "short", day: "numeric", year: "numeric" }).format(new Date());
    showView("dashboard");
  }

  init();
})();