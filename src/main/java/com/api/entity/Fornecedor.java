package com.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(
        name = "tb_fornecedor",
        indexes = {
                @Index(name = "idx_fornecedor_nome", columnList = "nome")
        }
)
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Nullable
    private String whatsapp;

    @Column(name = "cfp_cnpj")
    @JsonProperty("cpfCnpj")
    @Nullable
    private String cpfCnpj;

    private LocalDate dataCadastro;
}
