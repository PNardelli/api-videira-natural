package com.api.dto;

import com.api.eNum.StatusPedido;
import com.api.eNum.TipoMovimentacao;
import com.api.eNum.TipoVenda;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class VendaDTO {
    private Long clienteId; // ID do cliente selecionado no autocomplete
    private List<ItemVendaDTO> itens;
    private BigDecimal desconto;
    private BigDecimal subtotal;
    private BigDecimal totalFinal;
    private TipoVenda canalVenda;
    private StatusPedido statusPedido;
    private String metodoPagamento; // Ex: "PIX", "CARTAO", "DINHEIRO"

}