package com.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        // Use OriginPatterns para evitar erros de permissão estrita
        config.setAllowedOriginPatterns(Arrays.asList(
                "https://api-videira-natural-develop.up.railway.app",
                "https://www.videiranatural.com.br",
                "https://app.videiranatural.com.br",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}