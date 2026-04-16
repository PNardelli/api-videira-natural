package com.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tb_orcamento", indexes = {
        @Index(name = "idx_codigo_recuperacao", columnList = "codigoRecuperacao")
})
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 8)
    private String codigoRecuperacao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    private Cliente cliente;

    // Usamos ElementCollection para itens simples, evitando uma tabela extra complexa
    @ElementCollection
    @CollectionTable(name = "tb_orcamento_itens", joinColumns = @JoinColumn(name = "orcamento_id"))
    private List<OrcamentoItemEmbeddable> itens = new ArrayList<>();
}

