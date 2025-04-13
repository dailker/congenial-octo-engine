package com.humber.Tasky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.humber.Tasky")
public class TaskyApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskyApplication.class, args);
    }
}