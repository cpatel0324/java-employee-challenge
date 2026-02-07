package com.reliaquest.api.client;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class EmployeeApiClientTest {

    private static final String BASE_URL = "http://localhost:8112/api/v1/employee";

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private EmployeeApiClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new EmployeeApiClient(restTemplate, BASE_URL);
    }

    @Test
    void getAllEmployees_parsesWrapper() {
        server
                .expect(once(), requestTo(BASE_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "data": [
                                    {
                                      "id": "1",
                                      "employee_name": "Tiger Nixon",
                                      "employee_salary": 320800,
                                      "employee_age": 61,
                                      "employee_title": "VP",
                                      "employee_email": "tnixon@company.com"
                                    }
                                  ],
                                  "status": "Successfully processed request."
                                }
                                """,
                                MediaType.APPLICATION_JSON));

        List<Employee> employees = client.getAllEmployees();

        assertThat(employees).hasSize(1);
        assertThat(employees.get(0).getEmployeeName()).isEqualTo("Tiger Nixon");
        server.verify();
    }

    @Test
    void getEmployeeById_parsesWrapper() {
        server
                .expect(once(), requestTo(BASE_URL + "/123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "data": {
                                    "id": "123",
                                    "employee_name": "Bill Bob",
                                    "employee_salary": 89750,
                                    "employee_age": 24,
                                    "employee_title": "Engineer",
                                    "employee_email": "billBob@company.com"
                                  },
                                  "status": "ok"
                                }
                                """,
                                MediaType.APPLICATION_JSON));

        Employee e = client.getEmployeeById("123");

        assertThat(e.getId()).isEqualTo("123");
        assertThat(e.getEmployeeName()).isEqualTo("Bill Bob");
        server.verify();
    }

    @Test
    void createEmployee_postsBodyAndParsesWrapper() {
        CreateEmployeeInput input = new CreateEmployeeInput("Jill", 1000, 30, "Analyst");

        server
                .expect(once(), requestTo(BASE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(
                        content()
                                .json(
                                        """
                                        {"name":"Jill","salary":1000,"age":30,"title":"Analyst"}
                                        """))
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "data": {
                                    "id": "999",
                                    "employee_name": "Jill",
                                    "employee_salary": 1000,
                                    "employee_age": 30,
                                    "employee_title": "Analyst",
                                    "employee_email": "jill@company.com"
                                  },
                                  "status": "ok"
                                }
                                """,
                                MediaType.APPLICATION_JSON));

        Employee created = client.createEmployee(input);

        assertThat(created.getId()).isEqualTo("999");
        assertThat(created.getEmployeeName()).isEqualTo("Jill");
        server.verify();
    }

    @Test
    void deleteEmployeeByName_encodesPathAndSendsBody() {

        server
                .expect(once(), requestTo(BASE_URL + "/Bill%20Bob"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(
                        content().json(
                                """
                                {
                                  "name": "Bill Bob"
                                }
                                """
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "data": true,
                                  "status": "ok"
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );

        boolean deleted = client.deleteEmployeeByName("Bill Bob");

        assertThat(deleted).isTrue();
        server.verify();
    }


    @Test
    void getAllEmployees_retriesOn429ThenSucceeds() {
        // ordered expectations: 429 then success
        server
                .expect(once(), requestTo(BASE_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        server
                .expect(once(), requestTo(BASE_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess(
                                """
                                {"data":[{"id":"1","employee_name":"A","employee_salary":1,"employee_age":20,"employee_title":"t","employee_email":"a@c.com"}],"status":"ok"}
                                """,
                                MediaType.APPLICATION_JSON));

        List<Employee> employees = client.getAllEmployees();

        assertThat(employees).hasSize(1);
        server.verify();
    }
}
