package com.api.controller.IA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ia")
public class GeminiBotController {

    @Autowired
    private RestTemplate restTemplate;

    // Pode injetar via application.properties: gemini.api.key
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @PostMapping("/perguntar")
    public ResponseEntity<?> perguntarAoGemini(@RequestBody Map<String, String> payload) {
        String pergunta = payload.get("pergunta");

        // URL oficial do modelo Gemini 1.5 Flash (rápido e ideal para chat)
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // System Instruction + Pergunta do utilizador estructurada para o Gemini
        String promptCompleto = "Tu és o Videira Bot, um assistente virtual especialista em saúde, bem-estar, fitoterapia e suplementos naturais da loja 'Videira Natural'. " +
                "Dá respostas úteis, empáticas e concisas sobre chás, ervas e produtos naturais. Pergunta do cliente: " + pergunta;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", promptCompleto)
                        ))
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            // Extrai o texto da resposta do JSON de retorno do Gemini
            Map candidate = (Map) ((List) response.getBody().get("candidates")).get(0);
            Map content = (Map) candidate.get("content");
            Map part = (Map) ((List) content.get("parts")).get(0);
            String respostaTexto = (String) part.get("text");

            return ResponseEntity.ok(Map.of("resposta", respostaTexto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("resposta", "Desculpa, tive uma pequena instabilidade ao consultar o meu sistema de IA. Podes tentar novamente?"));
        }
    }
}