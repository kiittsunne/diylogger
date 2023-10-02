package com.jdsg.datalogger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataloggerApplication {

	public static void main(String[] args) {
		//TODO: Replace with logging facility in final.
		System.out.println("Application started!");
		SpringApplication.run(DataloggerApplication.class, args);
	}

}
