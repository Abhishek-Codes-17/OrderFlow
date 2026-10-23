package com.abhishek.orderflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OrderFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderFlowApplication.class, args);
	}
}