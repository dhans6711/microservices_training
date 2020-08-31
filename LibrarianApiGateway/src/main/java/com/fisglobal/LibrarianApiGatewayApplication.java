package com.fisglobal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class LibrarianApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibrarianApiGatewayApplication.class, args);
	}

}
