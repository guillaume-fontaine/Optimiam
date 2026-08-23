package fr.trollgun.optimiam;

import org.springframework.boot.SpringApplication;

public class TestOptimiamApplication {

	public static void main(String[] args) {
		SpringApplication.from(OptimiamApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
