package com.api.apiExterno.mercadoPago;

import com.api.dto.VendaMercadoPagoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pdv")
public class MercadoPagoController {

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @PostMapping("/cobrar")
    public ResponseEntity<?> cobrarNaMaquininha(@RequestBody VendaMercadoPagoDTO request) {

        ResponseEntity resposta = mercadoPagoService.criarOrdemPagamento(
                request.getValor(),
                request.getTipoPagamento(),
                request.getParcelas(),
                request.getExternalReference()
        );
        return ResponseEntity.status(resposta.getStatusCode()).body(resposta.getBody());
    }

    @GetMapping("/status-ordem/{orderID}")
    public ResponseEntity<?> verificarStatusOrdem(@PathVariable String orderID) {
        Map<String, Object> statusResponse = mercadoPagoService.consultarStatusPorReferencia(orderID);
        return ResponseEntity.ok(statusResponse);
    }

    @GetMapping("/pix-estatico/verificar")
    public ResponseEntity<?> verificarPixEstatico(
            @RequestParam Double valor,
            @RequestParam String inicio) {

        boolean pago = mercadoPagoService.verificarPixPago(valor, inicio);
        return ResponseEntity.ok(Map.of("paid", pago));
    }

    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<Void> receberNotificacao(
            @RequestBody(required = false) Map<String, Object> payload,
            @RequestParam(value = "type", required = false) String typeParam,
            @RequestParam(value = "topic", required = false) String topicParam,
            @RequestParam(value = "data.id", required = false) String dataIdParam,
            @RequestParam(value = "id", required = false) String idParam) {
        try {
            String tipo = typeParam != null ? typeParam : topicParam;
            String paymentId = dataIdParam != null ? dataIdParam : idParam;

            if (payload != null) {
                if (tipo == null && payload.get("type") != null) {
                    tipo = String.valueOf(payload.get("type"));
                }
                if (tipo == null && payload.get("topic") != null) {
                    tipo = String.valueOf(payload.get("topic"));
                }
                if (paymentId == null && payload.get("data") instanceof Map<?, ?> dataMap && dataMap.get("id") != null) {
                    paymentId = String.valueOf(dataMap.get("id"));
                }
            }

            if ("payment".equalsIgnoreCase(tipo) && paymentId != null && !paymentId.isBlank()) {
                mercadoPagoService.processarPagamentoAprovadoPorWebhook(paymentId);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Erro ao processar webhook do Mercado Pago: " + e.getMessage());
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/confirmar-online/{pedidoId}")
    public ResponseEntity<?> confirmarPagamentoOnline(
            @PathVariable Long pedidoId,
            @RequestParam(value = "paymentId", required = false) String paymentId) {
        boolean pago = mercadoPagoService.confirmarPagamentoPedidoOnline(pedidoId, paymentId);
        return ResponseEntity.ok(Map.of("pago", pago));
    }
}