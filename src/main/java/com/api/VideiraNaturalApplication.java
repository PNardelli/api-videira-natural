package com.api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
public class VideiraNaturalApplication {

	@PostConstruct
	public void init() {
		// Força o fuso horário da aplicação para o horário de Brasília (UTC-3)
		TimeZone.setDefault(TimeZone.getTimeZone("America/Recife"));
	}

	public static void main(String[] args) {
		SpringApplication.run(VideiraNaturalApplication.class, args);
	}
}
