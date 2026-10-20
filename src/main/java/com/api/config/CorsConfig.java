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

        config.setAllowedOriginPatterns(Arrays.asList(
                "https://api-videira-natural-develop.up.railway.app",
                "https://api-videira-natural-dev-production.up.railway.app",
                "https://api-videira-natural-prod-production.up.railway.app",
                "https://videiranatural.com.br",
                "https://www.videiranatural.com.br",
                "https://app.videiranatural.com.br",
                "https://app-videira-natural.vercel.app",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}