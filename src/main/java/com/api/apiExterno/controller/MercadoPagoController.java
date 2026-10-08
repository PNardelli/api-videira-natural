package com.api.apiExterno.controller;

import com.api.apiExterno.MercadoPagoService;
import com.api.apiExterno.PagamentoRequest;
import com.api.dto.VendaMercadoPagoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pdv")
public class MercadoPagoController {

    @Autowired
    private MercadoPagoService pointService;

    @PostMapping("/cobrar")
    public ResponseEntity<?> cobrarNaMaquininha(@RequestBody VendaMercadoPagoDTO request) {
        ResponseEntity<Map> resposta = pointService.criarOrdemPagamento(
                request.getValor(),
                converterNome(request.getTipoPagamento()),
                request.getExternalReference()
        );
        return ResponseEntity.status(resposta.getStatusCode()).body(resposta.getBody());
    }

    private String converterNome(String tipoPagamento) {
        if ("CARTAO_CREDITO".equals(tipoPagamento)) {
            return "credit_card";
        } else if ("CARTAO_DEBITO".equals(tipoPagamento)) {
            return "debit_card";
        }
        // Valor padrão caso venha vazio ou inesperado
        return "debit_card";
    }
}
