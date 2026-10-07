package com.api.dto.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmitenteDto {
    @JacksonXmlProperty(localName = "CNPJ")
    private String cnpj;

    @JacksonXmlProperty(localName = "xNome")
    private String nome;

    @JacksonXmlProperty(localName = "xFant")
    private String nomeFantasia;

    @JacksonXmlProperty(localName = "enderEmit")
    private EnderEmitDTO enderEmit;
}
