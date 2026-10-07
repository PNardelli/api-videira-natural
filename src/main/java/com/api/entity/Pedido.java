package com.api.entity;

import com.api.eNum.StatusPedido;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_pedidos")
@Data
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cliente;
    private String whatsapp;

    @Column(columnDefinition = "TEXT")
    private String produto;

    @Enumerated(EnumType.STRING)
    private StatusPedido etapa = StatusPedido.CRIADO; // Inicia como CRIADO

    private LocalDateTime dataHora = LocalDateTime.now();
}
