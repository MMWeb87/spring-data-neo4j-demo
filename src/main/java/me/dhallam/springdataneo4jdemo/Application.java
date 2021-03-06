package me.dhallam.springdataneo4jdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Import(RepositoryRestMvcConfiguration.class)
public class Application {
	
	public static final String BASE_PACKAGE = Application.class.getPackage().getName();
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
