package com.api.apiExterno.mercadoPago;

import com.api.dto.VendaMercadoPagoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

        String tipoConvertido = converterNome(request.getTipoPagamento());

        ResponseEntity<Map> resposta = mercadoPagoService.criarOrdemPagamento(
                request.getValor(),
                tipoConvertido,
                request.getParcelas(),
                request.getExternalReference()
        );
        return ResponseEntity.status(resposta.getStatusCode()).body(resposta.getBody());
    }

    private String converterNome(String tipoPagamento) {
        if ("CARTAO_CREDITO".equals(tipoPagamento) || "CREDITO_PARCELADO".equals(tipoPagamento)) {
            return "credit_card";
        } else if ("CARTAO_DEBITO".equals(tipoPagamento)) {
            return "debit_card";
        }
        // Valor padrão caso venha vazio ou inesperado
        return "debit_card";
    }

    @GetMapping("/status-ordem/{orderID}")
    public ResponseEntity<?> verificarStatusOrdem(@PathVariable String orderID) {
        // O seu pointService pode buscar a ordem no Mercado Pago pelo external_reference
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
    public ResponseEntity<Void> receberNotificacao(@RequestBody Map<String, Object> payload) {
        try {
            String tipo = (String) payload.get("type");

            // O Mercado Pago avisa quando há um novo pagamento
            if ("payment".equalsIgnoreCase(tipo)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                String paymentId = String.valueOf(data.get("id"));

                // Consulta a API do Mercado Pago usando o paymentId para ver os detalhes e o status
                // Se aprovado, localiza o pedido pela external_reference e atualiza o status para PAGO
                mercadoPagoService.processarPagamentoAprovadoPorWebhook(paymentId);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Erro ao processar webhook do Mercado Pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
