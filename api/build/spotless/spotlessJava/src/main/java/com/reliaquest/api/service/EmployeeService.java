package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeService {

    private final EmployeeApiClient client;

    public EmployeeService(EmployeeApiClient client) {
        this.client = client;
    }

    public List<Employee> getAllEmployees() {
        return client.getAllEmployees();
    }

    public List<Employee> searchByName(String fragment) {
        List<Employee> all = client.getAllEmployees();
        if (fragment == null || fragment.isBlank()) {
            return all;
        }
        String q = fragment.trim().toLowerCase(Locale.ROOT);
        return all.stream()
                .filter(e -> e.getEmployeeName() != null
                        && e.getEmployeeName().toLowerCase(Locale.ROOT).contains(q))
                .collect(Collectors.toList());
    }

    public Employee getById(String id) {
        if (id == null || id.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must not be blank");
        }
        try {
            return client.getEmployeeById(id);
        } catch (HttpClientErrorException.NotFound nf) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + id);
        }
    }

    public int highestSalary() {
        return client.getAllEmployees().stream()
                .map(Employee::getEmployeeSalary)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
    }

    public List<String> top10Names() {
        return client.getAllEmployees().stream()
                .filter(e -> e.getEmployeeSalary() != null)
                .sorted(Comparator.comparing(Employee::getEmployeeSalary).reversed())
                .limit(10)
                .map(Employee::getEmployeeName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Employee create(CreateEmployeeInput input) {
        if (input == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "employeeInput is required");
        }
        if (input.getName() == null || input.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank");
        }
        if (input.getSalary() == null || input.getSalary() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salary must be greater than zero");
        }
        if (input.getAge() == null || input.getAge() < 16 || input.getAge() > 75) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "age must be between 16 and 75");
        }
        if (input.getTitle() == null || input.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
        }
        return client.createEmployee(input);
    }

    public String deleteById(String id) {
        Employee emp = getById(id);
        String name = emp.getEmployeeName();
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Employee name missing for id=" + id);
        }
        boolean deleted = client.deleteEmployeeByName(name);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Delete failed for employee name=" + name);
        }
        return name;
    }
}
