package com.reliaquest.api.client;

import com.reliaquest.api.client.EmployeeApiResponses.DeleteResponse;
import com.reliaquest.api.client.EmployeeApiResponses.EmployeeListResponse;
import com.reliaquest.api.client.EmployeeApiResponses.EmployeeSingleResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class EmployeeApiClient {

    private static final Logger log = LoggerFactory.getLogger(EmployeeApiClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public EmployeeApiClient(
            RestTemplate restTemplate,
            @Value("${employee.mock.baseUrl:http://localhost:8112/api/v1/employee}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<Employee> getAllEmployees() {
        EmployeeListResponse body =
                with429Retry(() -> restTemplate.getForObject(baseUrl, EmployeeListResponse.class), "GET /employee");

        if (body == null || body.getData() == null) {
            throw new IllegalStateException("Upstream returned empty employee list");
        }
        return body.getData();
    }

    public Employee getEmployeeById(String id) {
        try {
            EmployeeSingleResponse body = with429Retry(
                    () -> restTemplate.getForObject(baseUrl + "/{id}", EmployeeSingleResponse.class, id),
                    "GET /employee/{id}");

            if (body == null || body.getData() == null) {
                throw new IllegalStateException("Upstream returned empty employee");
            }
            return body.getData();
        } catch (HttpClientErrorException.NotFound e) {
            throw e; // service/controller will map to 404
        }
    }

    public Employee createEmployee(CreateEmployeeInput input) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateEmployeeInput> entity = new HttpEntity<>(input, headers);

        EmployeeSingleResponse body = with429Retry(
                () -> restTemplate
                        .exchange(baseUrl, HttpMethod.POST, entity, EmployeeSingleResponse.class)
                        .getBody(),
                "POST /employee");

        if (body == null || body.getData() == null) {
            throw new IllegalStateException("Upstream returned empty created employee");
        }
        return body.getData();
    }

    public boolean deleteEmployeeByName(String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("name", name), headers);

        DeleteResponse body = with429Retry(
                () -> restTemplate
                        .exchange(baseUrl + "/{name}", HttpMethod.DELETE, entity, DeleteResponse.class, name)
                        .getBody(),
                "DELETE /employee/{name}");

        return body != null && Boolean.TRUE.equals(body.getData());
    }

    private <T> T with429Retry(SupplierWithRestClient<T> supplier, String opName) {
        int maxAttempts = 4;
        long backoffMs = 200;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return supplier.get();
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt == maxAttempts) {
                    log.warn("Rate limited on {} after {} attempts", opName, attempt);
                    throw e;
                }
                log.info("Rate limited on {} attempt {}. Backing off {}ms", opName, attempt, backoffMs);
                sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, Duration.ofSeconds(2).toMillis());
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during backoff");
        }
    }

    @FunctionalInterface
    private interface SupplierWithRestClient<T> {
        T get() throws RestClientException;
    }
}
