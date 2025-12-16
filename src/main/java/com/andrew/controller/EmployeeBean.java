package com.andrew.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import com.andrew.dao.EmployeeDAO;
import com.andrew.model.Employee;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named
@ViewScoped
public class EmployeeBean implements Serializable {

    private static final long serialVersionUID = 1L;
    @Getter @Setter
    private List<Employee> employees;

    @Getter @Setter
    private Employee currentEmployee; // Nhân viên đang được Thêm hoặc Sửa
    @Getter @Setter
    private boolean formVisible; // Kiểm soát việc hiện Form hay hiện Bảng
    @Getter @Setter
    private boolean isEditMode; // Kiểm soát xem đang là hành động Sửa hay Thêm mới

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

    // 1. Chuẩn bị form Thêm mới
    public void showAddForm() {
        this.currentEmployee = new Employee();
        this.isEditMode = false;
        this.formVisible = true;
    }

    // 2. Chuẩn bị form Sửa
    public void showEditForm(Employee emp) {
        // Nên clone object để tránh thay đổi trực tiếp trên bảng khi chưa Save
        this.currentEmployee = new Employee(
            emp.getEmployeeCode(), 
            emp.getEmployeeName(), 
            emp.getEmployeeAge(), 
            emp.getDateOfBirth()
        );
        this.isEditMode = true;
        this.formVisible = true;
    }

    // 3. Lưu (Gọi khi bấm nút Save trên Form)
    public void save() {
        if (currentEmployee.getDateOfBirth() != null) {
            // Chuyển java.util.Date sang java.time.LocalDate để tính toán
            LocalDate birthDate = currentEmployee.getDateOfBirth().toInstant()
                                  .atZone(ZoneId.systemDefault())
                                  .toLocalDate();
            LocalDate now = LocalDate.now();
            
            // Tính khoảng cách năm
            int age = Period.between(birthDate, now).getYears();
            
            // Gán ngược lại vào model
            currentEmployee.setEmployeeAge(age);
        } else {
            currentEmployee.setEmployeeAge(0);
        }
    	
        if (isEditMode) {
            employeeDAO.updateEmployee(currentEmployee);
        } else {
            employeeDAO.insertEmployee(currentEmployee);
        }
        loadEmployees(); // Tải lại danh sách
        this.formVisible = false; // Ẩn form, hiện bảng
    }

    // 4. Xóa
    public void delete(Employee emp) {
        employeeDAO.deleteEmployee(emp.getEmployeeCode());
        loadEmployees();
    }

    // 5. Hủy bỏ
    public void cancel() {
        this.formVisible = false;
        this.currentEmployee = null;
    }
}