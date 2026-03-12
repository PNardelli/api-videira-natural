package com.api.dto;

import com.api.entity.Endereco;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClienteDTO {

    private String nome;
    private String email;
    private String whatsapp;
    private Boolean ativo;
    private Boolean possuiConta;
    private Endereco endereco;

}
