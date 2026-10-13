package com.api.dto.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProdutoXmlDTO {

    @JacksonXmlProperty(localName = "cProd")
    private String codigoProduto;

    @JacksonXmlProperty(localName = "cEAN")
    private String codigoBarra;

    @JacksonXmlProperty(localName = "xProd")
    private String nome;

    @JacksonXmlProperty(localName = "qCom")
    private BigDecimal quantidade;

    @JacksonXmlProperty(localName = "vUnCom")
    private BigDecimal valorCusto;

    @JacksonXmlProperty(localName = "uCom")
    private String unidadeMedida;
}
