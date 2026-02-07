package com.reliaquest.api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpStatus;

@WebMvcTest(controllers = EmployeeController.class)
class EmployeeControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean private EmployeeService service;

    @Test
    void getAllEmployees_returns200AndList() throws Exception {
        Employee e = employee("1", "Tiger Nixon", 100);
        when(service.getAllEmployees()).thenReturn(List.of(e));

        mvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].employee_name").value("Tiger Nixon"));

        verify(service).getAllEmployees();
    }

    @Test
    void search_returns200AndFilteredList() throws Exception {
        Employee e = employee("1", "Tiger Nixon", 100);
        when(service.searchByName("ma")).thenReturn(List.of(e));

        mvc.perform(get("/api/v1/employee/search/ma"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee_name").value("Tiger Nixon"));

        verify(service).searchByName("ma");
    }

    @Test
    void getById_returns200() throws Exception {
        Employee e = employee("abc", "Bill Bob", 200);
        when(service.getById("abc")).thenReturn(e);

        mvc.perform(get("/api/v1/employee/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("abc"))
                .andExpect(jsonPath("$.employee_name").value("Bill Bob"));

        verify(service).getById("abc");
    }

    @Test
    void highestSalary_returnsInteger() throws Exception {
        when(service.highestSalary()).thenReturn(320800);

        mvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("320800"));

        verify(service).highestSalary();
    }

    @Test
    void topTen_returnsListOfNames() throws Exception {
        when(service.top10Names()).thenReturn(List.of("A", "B"));

        mvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("A"))
                .andExpect(jsonPath("$[1]").value("B"));

        verify(service).top10Names();
    }

    @Test
    void createEmployee_returnsCreatedEmployee() throws Exception {
        CreateEmployeeInput input = new CreateEmployeeInput("Jill", 1000, 30, "Analyst");
        Employee created = employee("999", "Jill", 1000);

        when(service.create(any())).thenReturn(created);

        mvc.perform(
                        post("/api/v1/employee")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("999"))
                .andExpect(jsonPath("$.employee_name").value("Jill"));

        verify(service).create(any());
    }

    @Test
    void deleteById_returnsName() throws Exception {
        when(service.deleteById("abc")).thenReturn("Bill Bob");

        mvc.perform(delete("/api/v1/employee/abc"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bill Bob"));

        verify(service).deleteById("abc");
    }

    private static Employee employee(String id, String name, Integer salary) {
        Employee e = new Employee();
        e.setId(id);
        e.setEmployeeName(name);
        e.setEmployeeSalary(salary);
        return e;
    }
}
