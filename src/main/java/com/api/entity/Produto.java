package com.api.entity;

import com.api.eNum.UnidadeMedida;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "TB_PRODUTO")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @Nullable
    private String codigoProduto;

    @Column(unique = true)
    @Nullable
    private String codigoBarras;

    @NotNull
    private String nome;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private UnidadeMedida unidadeMedida;

    @NotNull
    private BigDecimal precoVenda;

    private boolean ativo = true;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate dataCriacao;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate ultimaAtualizacao;

    @Nullable
    private String observacao;

    @Nullable
    private String codigoFornecedorXml;

    @OneToMany(mappedBy = "produto", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("produto")
    private List<ProdutoFornecedor> fornecedores;

    private String imagemUrl;

}
