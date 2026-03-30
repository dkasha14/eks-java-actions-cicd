package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "Java App deployed via EKS CI/CD ";
    }

    @GetMapping("/health")
    public String health() {
        return "Application is healthy ✅";
    }
}
