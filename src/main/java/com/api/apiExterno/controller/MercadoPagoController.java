package com.api.apiExterno.controller;

import com.api.apiExterno.MercadoPagoService;
import com.api.apiExterno.PagamentoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MercadoPagoController {

    @Autowired
    private MercadoPagoService service;

    @PostMapping("/maquininha")
    public ResponseEntity<?> pagar(@RequestBody PagamentoRequest req) {
        try {
            var pagamento = service.criarPagamentoMaquininha(
                    req.getValor(),
                    "Videira Natural - PDV",
                    req.getDeviceId()
            );

            return ResponseEntity.ok(pagamento);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/webhooks/mercado-pago")
    public ResponseEntity<Void> receberNotificacao(@RequestBody String payload) {
        // 1. Validar se o pagamento foi aprovado
        // 2. Se aprovado, atualizar o status da venda no seu banco de dados (PostgreSQL)
        // 3. Opcional: Usar WebSocket para avisar o Vue em tempo real
        return ResponseEntity.ok().build();
    }

}
