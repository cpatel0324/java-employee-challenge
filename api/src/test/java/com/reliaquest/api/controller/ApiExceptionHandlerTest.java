package com.reliaquest.api.controller;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleStatus_mapsResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must not be blank");

        ApiExceptionHandler.ErrorBody body = handler.handleStatus(ex);

        assertThat(body.status()).isEqualTo(400);
        assertThat(body.message()).contains("id must not be blank");
    }

    @Test
    void handleRateLimit_maps429To503() {
        HttpClientErrorException ex =
                HttpClientErrorException.create(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "rate limited",
                        new HttpHeaders(),
                        "rate limited".getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8);

        ApiExceptionHandler.ErrorBody body =
                handler.handleRateLimit((HttpClientErrorException.TooManyRequests) ex);

        assertThat(body.status()).isEqualTo(503);

        // pick the one that matches your handler implementation:
        // if you return a fixed message:
        assertThat(body.message()).contains("Upstream rate limited");
        // if you return exception message instead, use:
        // assertThat(body.message()).contains("rate limited");
    }

    @Test
    void handleUpstream_mapsRestClientExceptionTo502() {
        RestClientException ex = new RestClientException("boom");

        ApiExceptionHandler.ErrorBody body = handler.handleUpstream(ex);

        assertThat(body.status()).isEqualTo(502);
        assertThat(body.message()).contains("Upstream call failed");
    }

    @Test
    void handleIllegalState_mapsTo502() {
        IllegalStateException ex = new IllegalStateException("Upstream returned empty employee list");

        ApiExceptionHandler.ErrorBody body = handler.handleIllegalState(ex);

        assertThat(body.status()).isEqualTo(502);
        assertThat(body.message()).contains("Upstream returned empty employee list");
    }
}
