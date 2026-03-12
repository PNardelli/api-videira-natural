package com.api.entity;

import com.api.eNum.UnidadeMedida;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "TB_PRODUTO")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String codigoProduto;

    @Nullable
    private String codigoBarras;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private BigDecimal precoVenda;

    @OneToMany(mappedBy = "produto")
    private List<ProdutoFornecedor> fornecedores;

    @Enumerated(EnumType.STRING)
    private UnidadeMedida unidadeMedida;

    private LocalDate dataValidade;

    private Boolean ativo = true;
    private LocalDateTime dataCriacao;
    private String imagemUrl;

}
