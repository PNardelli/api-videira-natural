package com.api.dto;

import com.api.entity.Endereco;
import com.api.entity.ItemPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        Long clienteId,
        String clienteNome,
        String whatsapp,
        String produto,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cep,
        BigDecimal subtotal,
        BigDecimal desconto,
        BigDecimal totalFinal,
        BigDecimal valorFrete,
        String canalVenda,
        String tipoEntrega,
        String etapa,
        LocalDateTime dataHora,
        List<ItemPedido> itens
) {}
