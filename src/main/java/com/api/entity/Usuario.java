package com.api.entity;

import com.api.eNum.UsuarioRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "tb_usuario")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor // <-- Esta anotação do Lombok cria o construtor com todos os argumentos
@EqualsAndHashCode(of = "id")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;

    @Column(name = "password")
    private String password;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsuarioRole role;

    public Usuario(String login, String encryptedPassword, UsuarioRole role) {
    }
}
