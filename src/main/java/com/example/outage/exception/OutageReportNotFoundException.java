package com.example.outage.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested outage report cannot be found in the database.
 * Results in a 404 Not Found HTTP response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class OutageReportNotFoundException extends RuntimeException {

    public OutageReportNotFoundException(Long id) {
        super("Outage report not found with ID: " + id);
    }

    public OutageReportNotFoundException(String message) {
        super(message);
    }
}
