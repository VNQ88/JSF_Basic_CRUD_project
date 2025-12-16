package com.andrew.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.andrew.model.Employee;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped // Bean này sống suốt vòng đời ứng dụng, tiết kiệm tài nguyên
public class EmployeeDAO {
	@Inject
	@ConfigProperty(name = "db.url")
	private String dbUrl;

	@Inject
	@ConfigProperty(name = "db.username")
	private String dbUser;

	@Inject
	@ConfigProperty(name = "db.password")
	private String dbPassword;

	@Inject
	@ConfigProperty(name = "db.driverClass")
	private String dbDriver;

	// Hàm lấy danh sách nhân viên
	public List<Employee> getAllEmployees() {
	    List<Employee> list = new ArrayList<>();
	    String sql = "SELECT * FROM Mt_employee";

	    try {
	        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
	             PreparedStatement stmt = conn.prepareStatement(sql);
	             ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                Employee emp = new Employee();
	                emp.setEmployeeCode(rs.getString("employee_code"));
	                emp.setEmployeeName(rs.getString("employee_name"));
	                emp.setEmployeeAge(rs.getInt("employee_age"));
	                emp.setDateOfBirth(rs.getDate("date_of_birth"));
	                list.add(emp);
	            }
	        }
	    } catch (Exception e) {
	        log.error("Lỗi khi lấy dữ liệu nhân viên", e);
	    }	    
	    return list;
	}
	
	public void insertEmployee(Employee emp) {
        String sql = "INSERT INTO Mt_employee (employee_code, employee_name, employee_age, date_of_birth) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, emp.getEmployeeCode());
            stmt.setString(2, emp.getEmployeeName());
            stmt.setInt(3, Integer.valueOf(emp.getEmployeeAge()));
            if (emp.getDateOfBirth() != null) {
                stmt.setDate(4, new java.sql.Date(emp.getDateOfBirth().getTime())); 
            } else {
                stmt.setDate(4, null);
            } // Convert Date
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Lỗi khi thêm nhân viên", e);
            throw new RuntimeException(e); // Ném lỗi để Bean biết
        }
    }
	
	public void updateEmployee(Employee emp) {
        String sql = "UPDATE Mt_employee SET employee_name=?, employee_age=?, date_of_birth=? WHERE employee_code=?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, emp.getEmployeeName());
            stmt.setInt(2, emp.getEmployeeAge());
            stmt.setDate(3, new java.sql.Date(emp.getDateOfBirth().getTime()));
            stmt.setString(4, emp.getEmployeeCode()); // Where clause
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật nhân viên", e);
        }
    }
	
	public void deleteEmployee(String employeeCode) {
        String sql = "DELETE FROM Mt_employee WHERE employee_code=?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employeeCode);
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Lỗi khi xóa nhân viên", e);
        }
    }
}
