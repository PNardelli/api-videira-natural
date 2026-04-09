package com.api.dto;

import com.api.eNum.UnidadeMedida;
import com.api.entity.Fornecedor;
import jakarta.annotation.Nullable;
import lombok.Data;
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
    private Long categoriaId;
    private String descricao;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;
    private BigDecimal estoque;
    private UnidadeMedida unidadeMedida;
    private LocalDate dataValidade;
    private Long dataValidadeDias;
    private Long codigoFornecedorXml;
    private Long fornecedorId;
    private String observacao;

}