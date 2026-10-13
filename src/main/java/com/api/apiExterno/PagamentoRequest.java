package com.api.apiExterno;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PagamentoRequest {
    private BigDecimal valor;
    private String deviceId;
}