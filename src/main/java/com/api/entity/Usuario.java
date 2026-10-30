package com.api.entity;

import com.api.eNum.UsuarioRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "tb_usuario")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // <-- Esta anotação do Lombok cria o construtor com todos os argumentos
@EqualsAndHashCode(of = "id")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", unique = true)
    private String login;

    @Column(name = "password")
    private String password;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsuarioRole role;

    @OneToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", nullable = true)
    private Cliente cliente;

    private LocalDateTime ultimoAcesso;

    @Column(name = "token_acesso")
    private String tokenAcesso;

    @Column(name = "token_expiracao")
    private LocalDateTime tokenExpiracao;

    private Boolean ativo = true; // Útil caso precise banir ou desativar o acesso
}
