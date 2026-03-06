package com.example.qlns.DTO;

import com.example.qlns.Entity.Department;
import com.example.qlns.Entity.Employee;
import java.time.LocalDate;

public class CreateEmployeeRequest {

    private String fullName;
    private String employeeCode;
    private String email;
    private String password;
    private String phone;
    private String position;
    private String nationalId;
    private String address;
    private Double salary;
    private Employee.ContractType contractType;
    private Employee.Gender gender;
    private LocalDate birthDate;
    private LocalDate startDate;
    private Department department;

    public Employee toEmployee() {
        Employee emp = new Employee();
        emp.setFullName(fullName);
        emp.setEmployeeCode(employeeCode);
        emp.setPhone(phone);
        emp.setPosition(position);
        emp.setNationalId(nationalId);
        emp.setAddress(address);
        emp.setSalary(salary);
        emp.setContractType(contractType != null ? contractType : Employee.ContractType.FULL_TIME);
        emp.setGender(gender);
        emp.setBirthDate(birthDate);
        emp.setStartDate(startDate);
        emp.setDepartment(department);
        return emp;
    }

    // Getters
    public String getFullName() { return fullName; }
    public String getEmployeeCode() { return employeeCode; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public String getPosition() { return position; }
    public String getNationalId() { return nationalId; }
    public String getAddress() { return address; }
    public Double getSalary() { return salary; }
    public Employee.ContractType getContractType() { return contractType; }
    public Employee.Gender getGender() { return gender; }
    public LocalDate getBirthDate() { return birthDate; }
    public LocalDate getStartDate() { return startDate; }
    public Department getDepartment() { return department; }
}