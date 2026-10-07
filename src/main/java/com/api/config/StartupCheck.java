package com.api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StartupCheck {

    @Value("${gemini.api.key:NAO_ENCONTRADA}")
    private String geminiApiKey;

    @PostConstruct
    public void init() {
        // Mostra apenas os primeiros e últimos caracteres por segurança nos logs
        boolean configurada = geminiApiKey != null && !geminiApiKey.equals("NAO_ENCONTRADA") && geminiApiKey.length() > 5;
        System.out.println("=== TESTE DE VARIAVEL RAILWAY ===");
        System.out.println("GEMINI_API_KEY está configurada? " + configurada);
        if (configurada) {
            System.out.println("Tamanho da chave carregada: " + geminiApiKey.length());
        }
    }
}