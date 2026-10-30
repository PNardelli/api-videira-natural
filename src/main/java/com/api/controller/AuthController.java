package com.api.controller;

import com.api.dto.AuthenticationDTO;
import com.api.entity.Usuario;
import com.api.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Chave secreta segura para assinar o token
    private final Key chaveSecreta = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @PostMapping("/login")
    public ResponseEntity<?> fazerLogin(@RequestBody AuthenticationDTO dadosLogin) {
        // O 'login' enviado pelo front é o whatsapp limpo
        Optional<Usuario> usuarioOpt = usuarioRepository.findByLogin(dadosLogin.login());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // Validação da senha (se estiver usando BCrypt no cadastro, use passwordEncoder.matches,
            // se estiver comparando texto puro conforme seu código anterior, mantenha o .equals)
            if (usuario.getPassword().equals(dadosLogin.senha())) {

                // Gera o JWT real válido por 24 horas
                String tokenJwt = Jwts.builder()
                        .setSubject(usuario.getLogin())
                        .claim("role", usuario.getRole())
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
                        .signWith(chaveSecreta)
                        .compact();

                // Monta a resposta contendo o token, a role e os dados do cliente vinculado
                Map<String, Object> resposta = new HashMap<>();
                resposta.put("token", tokenJwt);
                resposta.put("role", usuario.getRole() != null ? usuario.getRole().toString() : "CLIENTE");

                // Adiciona os dados do cliente para o front-end conseguir saudar o usuário na loja
                Map<String, Object> clienteMap = new HashMap<>();
                if (usuario.getCliente() != null) {
                    clienteMap.put("id", usuario.getCliente().getId());
                    clienteMap.put("nome", usuario.getCliente().getNome());
                    clienteMap.put("whatsapp", usuario.getCliente().getWhatsapp());
                    clienteMap.put("email", usuario.getCliente().getEmail());
                }
                resposta.put("cliente", clienteMap);

                return ResponseEntity.ok(resposta);
            }
        }

        return ResponseEntity.status(401).body("WhatsApp ou senha incorretos");
    }
}