package com.api.dto;

import com.api.eNum.UnidadeMedida;
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
    private String codigoBarras;
    private String nome;
    private Long categoriaId;
    private Long fornecedorId;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;
    private Integer estoque;
    private UnidadeMedida unidadeMedida;
    private LocalDate dataValidade;
    @Nullable
    private String observacao;

}