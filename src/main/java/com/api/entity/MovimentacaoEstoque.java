package com.api.entity;

import com.api.eNum.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "tb_movimentacao_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private BigDecimal quantidade;

    // Na sua entidade MovimentacaoEstoque:
    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipoMovimentacao;

    private BigDecimal precoVenda;

    private BigDecimal precoCompra;

    private String unidadeMedida;

    private LocalDate dataValidade;

    private String observacao;
    private LocalDateTime data;

    private String origemMovimentacao;

    private String notaFiscal;

}
