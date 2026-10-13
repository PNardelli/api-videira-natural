package com.api.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter @Setter
public class OrcamentoItemEmbeddable {
    private Long produtoId;
    private BigDecimal quantidade;
}
