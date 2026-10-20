package com.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VendaMercadoPagoDTO {

    private BigDecimal valor;
    private String tipoPagamento;
    private String externalReference;

}
