package com.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class FaixaFrete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "distancia_maxima_km")
    private BigDecimal distanciaMaximaKm;
    private BigDecimal valor;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracao_frete_id", nullable = false)
    private ConfiguracaoFrete configuracaoFrete;

}
