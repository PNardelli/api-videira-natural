package com.api.dto.historicoVendas;

import com.api.dto.ItemVendaDTO;
import com.api.dto.PagamentoDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class VendaHistoricoDTO {
    private Long id;
    private String dataHora;
    private String clienteNome;
    private BigDecimal subtotal;
    private BigDecimal desconto;
    private BigDecimal totalFinal;
    private String metodoPagamento;
    private List<ItemVendaDTO> itens;
    private List<PagamentoDTO> pagamentos;
}