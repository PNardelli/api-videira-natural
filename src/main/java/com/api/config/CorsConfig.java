package com.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://app-videira-natural.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // O OPTIONS aqui já resolve o Preflight
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Dica: mantém a permissão no navegador por 1 hora
    }
}