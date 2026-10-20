package com.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnderecoDTO {

    private String bairro;
    private String complemento;
    private String cep;
    private String logradouro;
    private String numero;
    private String localidade;
    private String estado;


}
