package com.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "TB_CLIENTE")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @NotNull
    @Column(name = "NOME")
    private String nome;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "WHATSAPP")
    private String whatsapp;

    @Column(name = "FLAG_ATIVO")
    private Boolean ativo;

    @Column(name = "DATA_CRIACAO")
    private String dataCriacao;

    @Column(name = "POSSUI_CONTA_APP")
    private Boolean possuiContaApp;

    @Column(name = "TB_CLIENTE_DATA_NASCIMENTO")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private String dataNascimento;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn( name = "endereco_id")
    private Endereco endereco;

}
