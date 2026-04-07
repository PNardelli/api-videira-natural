package com.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class xmlDTO {

    // Indica que as informações estão dentro da tag <prod>
    @JacksonXmlProperty(localName = "prod")
    private ProdutoDetalhe prod;

    // Métodos auxiliares para o seu Service não quebrar
    public String getNome() { return prod != null ? prod.getNome() : null; }
    public String getCodigoBarra() { return prod != null ? prod.getCodigoBarra() : null; }
    //public BigDecimal getQuantidade() { return prod != null ? prod.getQuantidade() : 0.0; }
    public String getUnidade() { return prod != null ? prod.getUnidade() : null; }
    public Double getPrecoCusto() { return prod != null ? prod.getValorUnitario() : 0.0; }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProdutoDetalhe {
        @JacksonXmlProperty(localName = "cProd")
        private String codigoFornecedor;

        @JacksonXmlProperty(localName = "cEAN") // Tag oficial do código de barras
        private String codigoBarra;

        @JacksonXmlProperty(localName = "xProd") // Tag oficial do nome do produto
        private String nome;

        @JacksonXmlProperty(localName = "uCom")
        private String unidade;

        @JacksonXmlProperty(localName = "qCom")
        private BigDecimal quantidade;

        @JacksonXmlProperty(localName = "vUnCom")
        private Double valorUnitario;
    }
}
