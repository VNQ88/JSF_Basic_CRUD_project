package com.andrew.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.andrew.model.Employee;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class EmployeeDAO {

	private static final String DEFAULT_EMPLOYEE_CODE = "EMP001";
	private static final int CODE_NUMBER_LENGTH = 3;

	@Inject
	@ConfigProperty(name = "db.url")
	private String dbUrl;

	@Inject
	@ConfigProperty(name = "db.username")
	private String dbUsername;

	@Inject
	@ConfigProperty(name = "db.password")
	private String dbPassword;

	@Inject
	@ConfigProperty(name = "db.driverClass")
	private String dbDriverClass;

	public List<Employee> getAllEmployees() {
		List<Employee> employees = new ArrayList<>();
		String SELECT_ALL_EMPLOYEES_QUERY = "SELECT * FROM Mt_employee ORDER BY employee_code";

		try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
				PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_EMPLOYEES_QUERY);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				Employee employee = new Employee();
				employee.setEmployeeCode(rs.getString("employee_code"));
				employee.setEmployeeName(rs.getString("employee_name"));
				employee.setEmployeeAge(rs.getInt("employee_age"));
				employee.setDateOfBirth(rs.getDate("date_of_birth"));
				employees.add(employee);
			}
		} catch (SQLException e) {
			log.error("Error retrieving employee data", e);
			throw new RuntimeException("Failed to retrieve employees", e);
		}

		return employees;
	}

	public void insertEmployee(Employee employee) {
		String newCode = generateNextEmployeeCode();
		employee.setEmployeeCode(newCode);

		String INSERT_EMPLOYEE_QUERY = "INSERT INTO Mt_employee (employee_code, employee_name, employee_age, date_of_birth) VALUES (?, ?, ?, ?)";

		try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
				PreparedStatement stmt = conn.prepareStatement(INSERT_EMPLOYEE_QUERY)) {

			stmt.setString(1, employee.getEmployeeCode());
			stmt.setString(2, employee.getEmployeeName());
			stmt.setInt(3, employee.getEmployeeAge());

			if (employee.getDateOfBirth() != null) {
				stmt.setDate(4, new java.sql.Date(employee.getDateOfBirth().getTime()));
			} else {
				stmt.setDate(4, null);
			}

			stmt.executeUpdate();

		} catch (SQLException e) {
			log.error("Error inserting employee", e);
			throw new RuntimeException("Failed to insert employee", e);
		}
	}

	public void updateEmployee(Employee employee) {
		String UPDATE_EMPLOYEE_BY_EMPLOYEE_CODE_QUERY = "UPDATE Mt_employee SET employee_name=?, employee_age=?, date_of_birth=? WHERE employee_code=?";

		try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
				PreparedStatement stmt = conn.prepareStatement(UPDATE_EMPLOYEE_BY_EMPLOYEE_CODE_QUERY)) {

			stmt.setString(1, employee.getEmployeeName());
			stmt.setInt(2, employee.getEmployeeAge());

			if (employee.getDateOfBirth() != null) {
				stmt.setDate(3, new java.sql.Date(employee.getDateOfBirth().getTime()));
			} else {
				stmt.setDate(3, null);
			}

			stmt.setString(4, employee.getEmployeeCode());

			int rowsAffected = stmt.executeUpdate();

			if (rowsAffected == 0) {
				log.warn("No employee found with code: {}", employee.getEmployeeCode());
			}

		} catch (SQLException e) {
			log.error("Error updating employee with code: {}", employee.getEmployeeCode(), e);
			throw new RuntimeException("Failed to update employee", e);
		}
	}

	public void deleteEmployee(String employeeCode) {
		String DELETE_EMPLOYEE_BY_EMPLOYEE_CODE_QUERY = "DELETE FROM Mt_employee WHERE employee_code=?";

		try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
				PreparedStatement stmt = conn.prepareStatement(DELETE_EMPLOYEE_BY_EMPLOYEE_CODE_QUERY)) {

			stmt.setString(1, employeeCode);
			int rowsAffected = stmt.executeUpdate();

			if (rowsAffected == 0) {
				log.warn("No employee found with code: {}", employeeCode);
			}

		} catch (SQLException e) {
			log.error("Error deleting employee with code: {}", employeeCode, e);
			throw new RuntimeException("Failed to delete employee", e);
		}
	}

	private String generateNextEmployeeCode() {
		String sql = "SELECT MAX(employee_code) FROM Mt_employee";
		String nextCode = DEFAULT_EMPLOYEE_CODE;

		try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				String currentMaxCode = rs.getString(1);

				if (currentMaxCode != null && currentMaxCode.length() > CODE_NUMBER_LENGTH) {
					String prefix = currentMaxCode.substring(0, CODE_NUMBER_LENGTH);
					String numberPart = currentMaxCode.substring(CODE_NUMBER_LENGTH);

					try {
						int number = Integer.parseInt(numberPart);
						number++;
						nextCode = String.format("%s%0" + CODE_NUMBER_LENGTH + "d", prefix, number);

					} catch (NumberFormatException e) {
						log.warn("Invalid employee code format for auto-increment: {}. Using default code.",
								currentMaxCode);
					}
				}
			}
		} catch (SQLException e) {
			log.error("Error generating employee code", e);
		}

		return nextCode;
	}
}