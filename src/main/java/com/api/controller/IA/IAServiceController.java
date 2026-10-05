package com.api.controller.IA;

import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/teste")
public class IAServiceController {

    @Autowired
    private RestTemplate restTemplate; // ou WebClient

    //@Value("${openai.api.key}")
    private String openaiApiKey;

    @PostMapping("/teste")
    public ResponseEntity<?> perguntarAoBot(@RequestBody Map<String, String> payload) {
        String pergunta = payload.get("pergunta");

        // Monta o payload para a API da OpenAI
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openaiApiKey);

        String promptSistema = "Tu és o Videira Bot, um assistente virtual especialista em saúde, bem-estar, fitoterapia e suplementos naturais da loja 'Videira Natural'. " +
                "Dá respostas úteis, empáticas e concisas sobre chás, ervas e produtos naturais. Se não souberes um dado médico exato, recomenda consultar um profissional.";

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", promptSistema),
                        Map.of("role", "user", "content", pergunta)
                ),
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            // Extrai a resposta do JSON da OpenAI
            Map choices = (Map) ((List) response.getBody().get("choices")).get(0);
            Map message = (Map) choices.get("message");
            String respostaTexto = (String) message.get("content");

            return ResponseEntity.ok(Map.of("resposta", respostaTexto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("resposta", "Desculpa, tive uma pequena instabilidade ao consultar a minha base de conhecimento de IA. Podes tentar novamente?"));
        }
    }
}
