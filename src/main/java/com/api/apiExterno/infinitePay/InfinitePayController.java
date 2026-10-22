package com.api.apiExterno.infinitePay;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/infinitepay")
public class InfinitePayController {

    @Autowired
    private InfinitePayService infinitePayService;

    @CrossOrigin(origins = "*") // Libera para testes locais do front-end
    @PostMapping("/criar-link")
    public ResponseEntity<?> criarLink(@RequestBody Map<String, Object> requestBody) {
        try {

            // Extrai o valor do body enviado pelo Vue (ou pega o total)
            Double valor = Double.valueOf(requestBody.get("valor").toString());
            String descricao = (String) requestBody.get("descricao");

            Map<String, Object> resultado = infinitePayService.criarLinkCheckout(valor, descricao);

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
