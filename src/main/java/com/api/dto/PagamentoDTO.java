package com.api.dto;

import com.api.eNum.FormaPagamento;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PagamentoDTO {

    private FormaPagamento tipo;
    private BigDecimal valor;
    private Integer parcela;

}
