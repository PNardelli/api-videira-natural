package com.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(
        name = "tb_mensalidade_sistema",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ano", "mes"})
)
public class MensalidadeSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false)
    private Integer mes; // 1 a 12

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean pago = Boolean.FALSE;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "data_referencia")
    private LocalDate dataReferencia;
}