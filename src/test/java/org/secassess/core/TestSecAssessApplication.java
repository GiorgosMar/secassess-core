package org.secassess.core;

import org.springframework.boot.SpringApplication;

public class TestSecAssessApplication {

	public static void main(String[] args) {
		SpringApplication.from(SecAssessApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
