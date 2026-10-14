package com.api.model;

import com.api.entity.Cliente;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_usuario_app")
@Data
public class UsuarioApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vincula o usuário do app diretamente ao Cliente correspondente
    @OneToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", nullable = false)
    private Cliente cliente;

    @Column(unique = true, nullable = false)
    private String login; // Número do WhatsApp limpo (ex: 81999999999)

    @Column(nullable = false)
    private String senha; // Criptografada com BCrypt

    private LocalDate dataCriacao = LocalDate.now();

    private LocalDateTime ultimoAcesso;

    private String tokenRecuperarAcesso;

    private Boolean ativo = true; // Útil caso precise banir ou desativar o acesso
}