package com.example.outage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Electricity Outage Reporting System.
 * A student DevOps project demonstrating Spring Boot, Docker, Jenkins, and Ansible.
 */
@SpringBootApplication
public class OutageReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutageReportingApplication.class, args);
    }
}
