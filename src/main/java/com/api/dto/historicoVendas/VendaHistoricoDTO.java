package com.api.dto.historicoVendas;

import com.api.entity.Venda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record VendaHistoricoDTO(
        Long id,
        LocalDateTime dataVenda,
        String clienteNome,
        String clienteWhatsapp,
        BigDecimal subtotal,
        BigDecimal desconto,
        BigDecimal frete,
        BigDecimal total,
        String canalVenda,
        String statusPedido,
        int quantidadeItens,
        List<ItemVendaDTO> itens,
        List<PagamentoVendaDTO> pagamentos
) {
    public record ItemVendaDTO(
            String produtoNome,
            BigDecimal quantidade,
            BigDecimal precoUnitario,
            String unidadeMedida,
            BigDecimal subtotal
    ) {}

    public record PagamentoVendaDTO(
            String tipo,
            BigDecimal valor
    ) {}

    // Método estático para converter a entidade Venda em DTO com segurança
    public static VendaHistoricoDTO de(Venda venda) {

        // Tipagem explícita aqui para evitar List<?>
        List<ItemVendaDTO> itensDto = venda.getItens() != null ? venda.getItens().stream()
                .map(i -> new ItemVendaDTO(
                        i.getProduto() != null ? i.getProduto().getNome() : "Produto",
                        i.getQuantidade(),
                        i.getPrecoUnitario(),
                        i.getProduto().getUnidadeMedida().toString(),
                        i.getSubtotal()
                ))
                .collect(Collectors.toList()) : java.util.List.of();

        // Tipagem explícita aqui também
        List<PagamentoVendaDTO> pagamentosDto = venda.getPagamentos() != null ? venda.getPagamentos().stream()
                .map(p -> new PagamentoVendaDTO(
                        p.getTipo() != null ? p.getTipo().name() : "DINHEIRO",
                        p.getValor()
                ))
                .collect(Collectors.toList()) : java.util.List.of();

        int totalItens = itensDto.size();

        return new VendaHistoricoDTO(
                venda.getId(),
                venda.getDataVenda(),
                venda.getCliente() != null ? venda.getCliente().getNome() : "VENDA SEM CLIENTE",
                venda.getCliente() != null ? venda.getCliente().getWhatsapp() : null,
                venda.getSubtotal(),
                venda.getDesconto(),
                venda.getFrete(),
                venda.getTotal(),
                venda.getCanalVenda() != null ? venda.getCanalVenda().name() : "PDV",
                venda.getStatusPedido() != null ? venda.getStatusPedido().name() : "FINALIZADO",
                totalItens,
                itensDto,
                pagamentosDto
        );
    }
}