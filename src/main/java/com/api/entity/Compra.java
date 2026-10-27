package com.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tb_compras")
@Setter
@Getter
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataHora = LocalDateTime.now();

    private String identificadorNota = "SEM NOTA";

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    private String status = "PENDENTE"; // PENDENTE, PARCIAL, RECEBIDA

    private LocalDate previsaoEntrega; // Nova previsão
    private LocalDate dataEntrega;     // Data em que foi efetivamente entregue

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCompra> itens;

}