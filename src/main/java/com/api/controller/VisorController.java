package com.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visor")
@CrossOrigin(origins = "*")
public class VisorController {

    private Map<String, Object> estadoAtualVenda = new HashMap<>();

    @PostMapping("/atualizar")
    public ResponseEntity<String> atualizarVisor(@RequestBody Map<String, Object> dadosVenda) {
        this.estadoAtualVenda = dadosVenda;
        return ResponseEntity.ok("Visor atualizado com sucesso");
    }

    @GetMapping("/status")
    public ResponseEntity<?> obterStatusVisor() {
        if (estadoAtualVenda.isEmpty() || !Boolean.TRUE.equals(estadoAtualVenda.get("vendaAberta"))) {
            Map<String, Object> estadoLimpo = new HashMap<>();
            estadoLimpo.put("vendaAberta", false);
            estadoLimpo.put("carrinho", List.of());
            estadoLimpo.put("subtotal", 0.0);
            estadoLimpo.put("desconto", 0.0);
            estadoLimpo.put("totalFinal", 0.0);

            return ResponseEntity.ok(estadoLimpo);
        }
        return ResponseEntity.ok(estadoAtualVenda);
    }
}