package com.example.training;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TrainingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingApplication.class, args);
		System.out.println("DB URL: " + System.getenv("PROD_DB_URL"));
		System.out.println("DB USER: " + System.getenv("PROD_DB_USERNAME"));
	}

}
