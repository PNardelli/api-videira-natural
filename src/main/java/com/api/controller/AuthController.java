package com.api.controller;

import com.api.dto.AuthenticationDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationDTO request) {
        // Como está aberto para teste, você pode apenas retornar um OK
        // e um token falso para o seu Front não travar
        Map<String, String> response = new HashMap<>();
        response.put("token", "token-de-teste-liberado");
        response.put("usuario", request.login());

        return ResponseEntity.ok(response);
    }
}
