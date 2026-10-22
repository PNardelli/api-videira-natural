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
        ResponseEntity<Map> resposta = mercadoPagoService.criarOrdemPagamento(
                request.getValor(),
                converterNome(request.getTipoPagamento()),
                request.getParcela(),
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


}
