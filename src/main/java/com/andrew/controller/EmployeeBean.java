package com.andrew.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.andrew.dao.EmployeeDAO;
import com.andrew.model.Employee;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named
@SessionScoped
public class EmployeeBean implements Serializable {

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
	private EmployeeDAO employeeDAO;

	@PostConstruct
	public void init() {
		loadEmployees();
		this.formVisible = false;
	}

	private void loadEmployees() {
		this.employees = employeeDAO.getAllEmployees();
	}

	public void showAddForm() {
		this.currentEmployee = new Employee();
		this.isEditMode = false;
		this.formVisible = true;
	}

	public void showEditForm(Employee emp) {
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
			employeeDAO.updateEmployee(currentEmployee);
		} else {
			employeeDAO.insertEmployee(currentEmployee);
		}
		loadEmployees();
		this.formVisible = false;
	}

	public void deleteEmployee(Employee emp) {
		employeeDAO.deleteEmployee(emp.getEmployeeCode());
		loadEmployees();
	}

	public void cancel() {
		this.formVisible = false;
		this.currentEmployee = null;
	}
}