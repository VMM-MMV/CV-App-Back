package com.example.demo;

import com.example.demo.services.ResumeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CVApplication {
	public static void main(String[] args) {
		Thread thread;
		try {
			thread = new Thread(new ResumeService());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		thread.start();
		SpringApplication.run(CVApplication.class, args);
	}
}