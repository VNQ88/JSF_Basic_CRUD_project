package com.andrew.beans;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;

import com.andrew.model.Employee;
import com.andrew.service.EmployeeService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named
@ConversationScoped
public class EmployeeBean implements Serializable {
	private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}]+([\\s\\-'][\\p{L}]+)*$");
	private static final int MIN_WORKING_AGE = 18;
	private static final int MAX_WORKING_AGE = 60;
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private List<Employee> employees;

	@Getter
	@Setter
	private Employee currentEmployee;
	@Getter
	@Setter
	private boolean formVisible;
	@Getter
	@Setter
	private boolean isEditMode;

	@Inject
	private EmployeeService employeeService;;

	@Inject
	private Conversation conversation;

	@PostConstruct
	public void init() {
		loadEmployees();
		this.formVisible = false;
	}

	@PreDestroy
	public void destroy() {
		log.info("EmployeeBean destroyed. cid={}", conversation == null ? "null" : conversation.getId());
	}

	private void beginConversation() {
		if (conversation.isTransient()) {
			conversation.begin();
			log.info("Conversation BEGIN, id={}", conversation.getId());
		}
	}

	private void endConversation() {
		if (!conversation.isTransient()) {
			log.info("Conversation END, id={}", conversation.getId());
			conversation.end();
		}
	}

	private void loadEmployees() {
		this.employees = employeeService.getAllEmployees();
	}

	public void showAddForm() {
		beginConversation();
		this.currentEmployee = new Employee();
		this.isEditMode = false;
		this.formVisible = true;
	}

	public void showEditForm(Employee emp) {
		beginConversation();
		this.currentEmployee = new Employee(emp.getEmployeeCode(), emp.getEmployeeName(), emp.getEmployeeAge(),
				emp.getDateOfBirth());
		this.isEditMode = true;
		this.formVisible = true;
	}

	public void saveEmployee() {
		if (currentEmployee.getDateOfBirth() != null) {
			LocalDate birthDate = currentEmployee.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault())
					.toLocalDate();
			LocalDate now = LocalDate.now();
			int age = Period.between(birthDate, now).getYears();
			currentEmployee.setEmployeeAge(age);
		} else {
			currentEmployee.setEmployeeAge(0);
		}

		if (isEditMode) {
			employeeService.updateEmployee(currentEmployee);
		} else {
			employeeService.insertEmployee(currentEmployee);
		}
		loadEmployees();
		this.formVisible = false;
		endConversation();
	}

	public void deleteEmployee(Employee emp) {
		employeeService.deleteEmployee(emp.getEmployeeCode());
		loadEmployees();
	}

	public void cancel() {
		this.formVisible = false;
		this.currentEmployee = null;
		endConversation();
	}

	public void fullNameValidator(FacesContext context, UIComponent component, String value) {
		if (value == null)
			return;

		String trimmed = value.trim();
		if (trimmed.isEmpty())
			return;

		if (!NAME_PATTERN.matcher(trimmed).matches()) {
			if (component instanceof EditableValueHolder) {
				((EditableValueHolder) component).setSubmittedValue(null);
			}
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid FullName",
					"Only letters, hyphens, apostrophes, and spaces are allowed."));
		}
	}

	public void dobRangeValidator(FacesContext context, UIComponent component, Date value) throws ValidatorException {
		if (value == null)
			return;
		LocalDate dob = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int birthYear = dob.getYear();
		int currentYear = LocalDate.now().getYear();

		int minYear = currentYear - MAX_WORKING_AGE;
		int maxYear = currentYear - MIN_WORKING_AGE;

		if (birthYear < minYear || birthYear > maxYear) {
			if (component instanceof EditableValueHolder) {
				((EditableValueHolder) component).setSubmittedValue(null);
			}
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Date of Birth",
					String.format("Year of birth must be between %d and %d.", minYear, maxYear)));
		}
	}
}