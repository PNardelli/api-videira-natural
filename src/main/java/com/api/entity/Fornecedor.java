package com.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "tb_fornecedor")
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String whatsapp;

    @Column(name = "cfp_cnpj")
    @JsonProperty("cpfCnpj")
    private String cpfCnpj;

    private LocalDate dataCadastro;
}
