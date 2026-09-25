package com.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_parceiros")
public class Parceiros {

    @Id
    private long id;

    private boolean ativo;
    private String nomeParceiro;
    private long percentualDesconto;

}
