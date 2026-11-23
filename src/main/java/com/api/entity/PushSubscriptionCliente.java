package com.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "tb_push_subscription_cliente")
public class PushSubscriptionCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(columnDefinition = "TEXT", nullable = false, unique = true)
    private String endpoint;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String p256dh;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String auth;

    private LocalDateTime atualizadoEm = LocalDateTime.now();
}