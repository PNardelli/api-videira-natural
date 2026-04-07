package com.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@JacksonXmlRootElement(localName = "nfeProc")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NfeCompletaDto {

    @JacksonXmlProperty(localName = "NFe")
    private NFe nfe;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NFe {
        @JacksonXmlProperty(localName = "infNFe")
        private InfNFe infNFe;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfNFe {
        @JacksonXmlProperty(localName = "emit") // Dados do Fornecedor
        private EmitenteDto emitente;

        @JacksonXmlProperty(localName = "det") // Lista de Itens
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<xmlDTO> itens;
    }
}