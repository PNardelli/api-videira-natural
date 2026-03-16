package com.api.controller;

import com.api.business.TokenService;
import com.api.dto.AuthenticationDTO;
import com.api.dto.LoginResponseDTO;
import com.api.dto.RegisterDTO;
import com.api.entity.Usuario;
import com.api.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((Usuario) auth.getPrincipal());

        var usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getRole().toString()));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterDTO data) {
        // Verifica se já existe um usuário com esse login
        if(this.repository.findByLogin(data.login()) != null) {
            return ResponseEntity.badRequest().build();
        }

        // Criptografa a senha usando BCrypt (Obrigatório!)
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.senha());

        // Cria o novo usuário com a Role definida (ADMIN ou USER)
        Usuario newUser = new Usuario(data.login(), encryptedPassword, data.role());

        // Salva no banco de dados
        this.repository.save(newUser);

        return ResponseEntity.ok().build();
    }
}
