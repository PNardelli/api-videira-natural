package com.api.entity;

import com.api.eNum.UsuarioRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsuarioRole role;

    public Usuario(String login, String encryptedPassword, UsuarioRole role) {
    }
}
