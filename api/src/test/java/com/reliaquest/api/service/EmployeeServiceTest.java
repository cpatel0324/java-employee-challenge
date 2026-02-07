package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

class EmployeeServiceTest {
    @Mock private EmployeeApiClient client;
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new EmployeeService(client);
    }

    @Test
    void getAllEmployees_delegatesToClient() {
        when(client.getAllEmployees()).thenReturn(List.of(employee("1", "A", 100)));

        List<Employee> result = service.getAllEmployees();

        assertThat(result).hasSize(1);
        verify(client).getAllEmployees();
    }

    @Test
    void searchByName_blank_returnsAll() {
        List<Employee> all = List.of(employee("1", "Tiger Nixon", 1), employee("2", "Bill Bob", 2));
        when(client.getAllEmployees()).thenReturn(all);

        assertThat(service.searchByName(null)).containsExactlyElementsOf(all);
        assertThat(service.searchByName("   ")).containsExactlyElementsOf(all);
    }

    @Test
    void searchByName_caseInsensitiveContains() {
        List<Employee> all = List.of(employee("1", "Tiger Nixon", 1), employee("2", "Bill Bob", 2));
        when(client.getAllEmployees()).thenReturn(all);

        List<Employee> result = service.searchByName("tIG");

        assertThat(result).extracting(Employee::getEmployeeName).containsExactly("Tiger Nixon");
    }

    @Test
    void getById_blank_throwsBadRequest() {
        assertThatThrownBy(() -> service.getById("  "))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(
                        ex ->
                                assertThat(((ResponseStatusException) ex).getStatusCode())
                                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void getById_upstream404_isPropagatedAsHttpClientErrorException() {
        when(client.getEmployeeById("missing"))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> service.getById("missing"))
                .isInstanceOf(HttpClientErrorException.class)
                .satisfies(
                        ex -> {
                            HttpClientErrorException hee = (HttpClientErrorException) ex;
                            assertThat(hee.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        });
    }

    @Test
    void getHighestSalary_returnsMaxOrZero() {
        when(client.getAllEmployees())
                .thenReturn(List.of(employee("1", "A", 10), employee("2", "B", 250), employee("3", "C", 100)));

        int max = service.highestSalary();

        assertThat(max).isEqualTo(250);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_returnsNamesSortedDescLimited10() {
        when(client.getAllEmployees())
                .thenReturn(
                        List.of(
                                employee("1", "A", 10),
                                employee("2", "B", 200),
                                employee("3", "C", 150),
                                employee("4", "D", 300)));

        List<String> names = service.top10Names();

        assertThat(names).containsExactly("D", "B", "C", "A");
    }

    @Test
    void createEmployee_validatesInput_badRequest() {
        CreateEmployeeInput bad = new CreateEmployeeInput("", 0, 10, "");
        assertThatThrownBy(() -> service.create(bad))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(
                        ex ->
                                assertThat(((ResponseStatusException) ex).getStatusCode())
                                        .isEqualTo(HttpStatus.BAD_REQUEST));
        verifyNoInteractions(client);
    }

    @Test
    void deleteEmployeeById_getsEmployeeThenDeletesByName_returnsName() {
        Employee emp = employee("abc", "Bill Bob", 100);
        when(client.getEmployeeById("abc")).thenReturn(emp);
        when(client.deleteEmployeeByName("Bill Bob")).thenReturn(true);

        String deleted = service.deleteById("abc");

        assertThat(deleted).isEqualTo("Bill Bob");
        verify(client).getEmployeeById("abc");
        verify(client).deleteEmployeeByName("Bill Bob");
    }

    @Test
    void deleteEmployeeById_deleteFalse_throwsBadGateway() {
        Employee emp = employee("abc", "Bill Bob", 100);
        when(client.getEmployeeById("abc")).thenReturn(emp);
        when(client.deleteEmployeeByName("Bill Bob")).thenReturn(false);

        assertThatThrownBy(() -> service.deleteById("abc"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(
                        ex ->
                                assertThat(((ResponseStatusException) ex).getStatusCode())
                                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    private static Employee employee(String id, String name, Integer salary) {
        Employee e = new Employee();
        e.setId(id);
        e.setEmployeeName(name);
        e.setEmployeeSalary(salary);
        return e;
    }
}
