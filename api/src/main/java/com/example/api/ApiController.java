package com.example.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(originPatterns = "*")
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/hello")
    @PreAuthorize("hasRole('user') or hasRole('admin')")
    public String hello() {
        return "Hello from Spring Boot!";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('admin')")
    public String admin() {
        return "Hello Admin!";
    }

}
