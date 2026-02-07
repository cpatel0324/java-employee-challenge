package com.reliaquest.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ErrorBody handleStatus(ResponseStatusException e) {
        return new ErrorBody(e.getStatusCode().value(), e.getReason());
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ErrorBody handleRateLimit(HttpClientErrorException.TooManyRequests e) {
        return new ErrorBody(HttpStatus.SERVICE_UNAVAILABLE.value(), "Upstream rate limited. Retry shortly.");
    }

    @ExceptionHandler(RestClientException.class)
    public ErrorBody handleUpstream(RestClientException e) {
        return new ErrorBody(HttpStatus.BAD_GATEWAY.value(), "Upstream call failed.");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ErrorBody handleIllegalState(IllegalStateException e) {
        return new ErrorBody(HttpStatus.BAD_GATEWAY.value(), e.getMessage());
    }

    public record ErrorBody(int status, String message) {}
}

