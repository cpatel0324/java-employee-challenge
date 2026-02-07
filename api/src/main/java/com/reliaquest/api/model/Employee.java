package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Employee {
    private String id;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("employee_salary")
    private Integer employeeSalary;

    @JsonProperty("employee_age")
    private Integer employeeAge;

    @JsonProperty("employee_title")
    private String employeeTitle;

    @JsonProperty("employee_email")
    private String employeeEmail;

    public Employee() {}

    public String getId() {
        return id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public Integer getEmployeeSalary() {
        return employeeSalary;
    }

    public Integer getEmployeeAge() {
        return employeeAge;
    }

    public String getEmployeeTitle() {
        return employeeTitle;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public void setEmployeeSalary(Integer employeeSalary) {
        this.employeeSalary = employeeSalary;
    }

    public void setEmployeeAge(Integer employeeAge) {
        this.employeeAge = employeeAge;
    }

    public void setEmployeeTitle(String employeeTitle) {
        this.employeeTitle = employeeTitle;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }
}
