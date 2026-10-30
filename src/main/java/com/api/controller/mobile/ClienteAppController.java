package com.api.controller.mobile;

import com.api.apiExterno.email.EmailService;
import com.api.business.ClienteService;
import com.api.dto.ClienteDTO;
import com.api.dto.ValidarTokenDTO;
import com.api.entity.Cliente;
import com.api.repository.ClienteRepository;
import com.api.uteis.TokenCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/cliente/app")
public class ClienteAppController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private TokenCacheService tokenCacheService;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ClienteRepository clienteRepository;

    @PostMapping("/cadastrar")
    public ResponseEntity<?> registrarClienteApp(@RequestBody ClienteDTO dto) {
        try {
            Cliente novoCliente = clienteService.cadastrarClienteViaApp(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoCliente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body((e.getMessage()));
        }
    }

    @GetMapping("/verificar")
    public ResponseEntity<?> verificarCliente(@RequestParam("whatsapp") String whatsapp) {
        try {
            // Limpa a máscara caso venha com formatação
            String whatsappLimpo = whatsapp.replaceAll("\\D", "");

            Cliente cliente = clienteRepository.buscarPorWhatsapp(whatsappLimpo).orElse(null);

            if (cliente == null) {
                // Retorna 404 para o front-end saber que é um cliente novo
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado");
            }
            // Se encontrou, retorna os dados necessários para preencher a tela
            return ResponseEntity.ok(cliente);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verificar-e-enviar")
    public ResponseEntity<?> verificarEEnviarToken(@RequestBody ClienteDTO dto) {
        try {
            String whatsappLimpo = dto.getWhatsapp().replaceAll("\\D", "");
            Cliente cliente = clienteRepository.buscarPorWhatsapp(whatsappLimpo).orElse(null);

            // Se já existe e já tem conta ativa no app, bloqueia
            if (cliente != null && cliente.getPossuiContaApp()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Este WhatsApp já possui uma conta ativa no aplicativo.");
            }

            // Define para qual e-mail enviar o token
            String emailDestino = (cliente != null && !cliente.getEmail().isEmpty()) ? cliente.getEmail() : dto.getEmail();

            if (emailDestino == null || emailDestino.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("E-mail obrigatório para o envio do token.");
            }

            // 1. Gera o token de 6 dígitos
            String token = String.format("%06d", new Random().nextInt(999999));

            // 2. Salva no cache
            tokenCacheService.salvarToken(whatsappLimpo, token);

            // 3. Dispara o e-mail
            emailService.enviarTokenEmail(emailDestino, token);

            // Retorna se existe e, se existir, envia o nome para preencher na tela
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("clienteExiste", (cliente != null));
            resposta.put("nome", (cliente != null) ? cliente.getNome() : null); // Ajuste para o método que pega o nome do seu cliente

            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/validar-token")
    public ResponseEntity<?> validarToken(@RequestBody ValidarTokenDTO dto) {
        String whatsappLimpo = dto.getWhatsapp().replaceAll("\\D", "");

        boolean tokenValido = tokenCacheService.validarToken(whatsappLimpo, dto.getToken());

        if (!tokenValido) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido ou expirado.");
        }

        return ResponseEntity.ok("Token validado com sucesso!");
    }

}
