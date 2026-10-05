package com.api.config; // Ajuste para o seu pacote atual

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Cria um gerenciador de cache em memória RAM leve e nativo do Spring
        return new ConcurrentMapCacheManager("categorias", "fornecedores");
    }
}