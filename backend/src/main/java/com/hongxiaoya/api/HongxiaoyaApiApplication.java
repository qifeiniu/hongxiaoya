package com.hongxiaoya.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HongxiaoyaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HongxiaoyaApiApplication.class, args);
	}

}
