package com.api.dto.requests;

import com.api.eNum.UnidadeMedida;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProdutoRequest {

    private String codigoProduto;
    private String codigoBarra;
    private String nome;
    private String descricao;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;
    private BigDecimal estoque;
    private UnidadeMedida unidadeMedida;
    private LocalDate dataValidade;
    private Long dataValidadeDias;
    private Long codigoFornecedorXml;
    private String observacao;

    private Long produtoId;
    private Long fornecedorId;
    private Long categoriaId;


}