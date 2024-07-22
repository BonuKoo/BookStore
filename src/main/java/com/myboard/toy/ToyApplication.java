package com.myboard.toy;

import com.myboard.toy.domain.hello.HelloUser;
import com.myboard.toy.domain.hello.service.HelloUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDate;

@EnableJpaAuditing
@SpringBootApplication
public class ToyApplication  {

	@Autowired
	private HelloUserService service;

	public static void main(String[] args) {
		SpringApplication.run(ToyApplication.class, args);
	}


}
