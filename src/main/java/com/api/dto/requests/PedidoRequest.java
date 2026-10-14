package com.api.dto.requests;

import com.api.dto.ClientePdvDTO;
import com.api.eNum.TipoEntrega;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PedidoRequest {

    private Long clienteId;
    private String clientePedidoNome;
    private String clientePedidoWhatsapp;

    @Enumerated(EnumType.STRING)
    private TipoEntrega tipoEntrega;
    private Long parceiroId; // Novo (opcional para parcerias)
    private List<ItemCarrinhoRequest> itens;
    private BigDecimal subtotal; // Novo
    private BigDecimal desconto;
    private BigDecimal acrescimo;
    private BigDecimal totalFinal; // Novo
    private String formaPagamento;
    private String canalVenda; // Novo ("PDV" ou "WHATSAPP_ONLINE")

    private ClientePdvDTO cliente;
}

