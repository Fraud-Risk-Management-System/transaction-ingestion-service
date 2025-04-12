package com.fraudrisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.fraudrisk")
public class TransactionIngestionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionIngestionServiceApplication.class, args);
	}

}
