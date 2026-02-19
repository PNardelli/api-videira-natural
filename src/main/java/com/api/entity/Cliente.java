package com.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "BD_CLIENTE_NOME")
    private String nome;

    @Column(name = "BD_CLIENTE_EMAIL")
    private String email;

    @Column(name = "BD_CLIENTE_WHATSAPP")
    private String whatsapp;

    @Column(name = "BD_FLAG_ATIVO")
    private Boolean ativo;

    @Column(name = "BD_CLIENTE_DATA_CRIACAO")
    private LocalDate dataCriacao;

    @Column(name = "BD_CLIENTE_POSSUI_CONTA")
    private Boolean possuiConta;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn( name = "endereco_id")
    private Endereco endereco;

}
