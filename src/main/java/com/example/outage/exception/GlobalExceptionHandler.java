package com.example.outage.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Centralized exception handler for the MVC layer.
 * Maps exceptions to user-friendly error pages.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles the case where a requested outage report does not exist.
     */
    @ExceptionHandler(OutageReportNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(OutageReportNotFoundException ex, Model model,
                                  HttpServletRequest request) {
        model.addAttribute("statusCode", 404);
        model.addAttribute("errorTitle", "Report Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/error";
    }

    /**
     * Catch-all handler for any unexpected server error.
     * AccessDeniedException is re-thrown so Spring Security handles it (returns 403).
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(Exception ex, Model model,
                                      HttpServletRequest request) throws Exception {
        // Let Spring Security handle access-denied errors (403)
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            throw ex;
        }
        model.addAttribute("statusCode", 500);
        model.addAttribute("errorTitle", "Unexpected Error");
        model.addAttribute("errorMessage", "Something went wrong. Please try again or contact support.");
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/error";
    }
}
