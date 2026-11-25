package com.api.controller;

import com.api.business.PushNotificationService;
import com.api.entity.PushSubscriptionCliente;
import com.api.repository.PushSubscriptionClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/loja/push")
@CrossOrigin(origins = "*")
public class PushController {

    @Value("${vapid.public.key}")
    private String publicKey;

    @Autowired
    private PushSubscriptionClienteRepository repository;

    @Autowired
    private PushNotificationService pushNotificationService;

    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        return Map.of("publicKey", publicKey);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> inscrever(@RequestBody Map<String, Object> body) {
        Long clienteId = Long.valueOf(body.get("clienteId").toString());
        Map<String, Object> subscription = (Map<String, Object>) body.get("subscription");
        Map<String, String> keys = (Map<String, String>) subscription.get("keys");

        String endpoint = subscription.get("endpoint").toString();

        PushSubscriptionCliente entity = repository.findByEndpoint(endpoint)
                .orElseGet(PushSubscriptionCliente::new);

        entity.setClienteId(clienteId);
        entity.setEndpoint(endpoint);
        entity.setP256dh(keys.get("p256dh"));
        entity.setAuth(keys.get("auth"));
        entity.setAtualizadoEm(LocalDateTime.now());

        repository.save(entity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/enviar")
    public ResponseEntity<?> enviarPushParaTodos(@RequestBody Map<String, String> body) {
        String titulo = body.get("title");
        String mensagem = body.get("body");
        String url = body.getOrDefault("url", "/app");

        if (titulo == null || titulo.isBlank() || mensagem == null || mensagem.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Título e mensagem são obrigatórios."));
        }

        int enviados = pushNotificationService.enviarParaTodos(titulo, mensagem, url);
        return ResponseEntity.ok(Map.of(
                "mensagem", "Push enviado com sucesso!",
                "totalEnviados", enviados
        ));
    }

}