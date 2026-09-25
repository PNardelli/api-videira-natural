package com.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Parceiros {

    @Id
    private long id;

    private boolean ativo;
    private String nomeParceiro;
    private long percentualDesconto;

}
