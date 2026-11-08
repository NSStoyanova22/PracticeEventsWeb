package com.example.hefest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HefestApplication {

	public static void main(String[] args) {
		SpringApplication.run(HefestApplication.class, args);
	}

}
