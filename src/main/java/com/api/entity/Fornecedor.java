package com.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cfpCnpj;
    private String whatsapp;

    @OneToMany(mappedBy = "fornecedor")
    private List<ProdutoFornecedor> produtos;

}
