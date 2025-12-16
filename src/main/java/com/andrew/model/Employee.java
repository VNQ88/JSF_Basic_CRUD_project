package com.andrew.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Employee {
    private String employeeCode;
    private String employeeName;
    private int employeeAge;
    private Date dateOfBirth;
   
}
