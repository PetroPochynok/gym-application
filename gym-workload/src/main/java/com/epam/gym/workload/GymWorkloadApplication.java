package com.epam.gym.workload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class GymWorkloadApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymWorkloadApplication.class, args);
	}

}
