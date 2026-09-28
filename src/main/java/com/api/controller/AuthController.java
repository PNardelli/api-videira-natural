package com.api.controller;

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

    // Chave secreta segura gerada dinamicamente para assinar o token
    private final Key chaveSecreta = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @PostMapping("/login")
    public ResponseEntity<?> fazerLogin(@RequestBody Usuario dadosLogin) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByLogin(dadosLogin.getLogin());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // Validação simples da senha
            if (usuario.getPassword().equals(dadosLogin.getPassword())) {

                // Gera um JWT real válido por 24 horas
                String tokenJwt = Jwts.builder()
                        .setSubject(usuario.getLogin())
                        .claim("role", usuario.getRole())
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
                        .signWith(chaveSecreta)
                        .compact();

                Map<String, Object> resposta = new HashMap<>();
                resposta.put("token", tokenJwt);
                resposta.put("role", usuario.getRole() != null ? usuario.getRole() : "ADMIN");

                return ResponseEntity.ok(resposta);
            }
        }

        return ResponseEntity.status(401).body("Usuário ou senha incorretos");
    }
}