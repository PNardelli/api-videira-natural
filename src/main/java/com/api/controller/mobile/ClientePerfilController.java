package com.api.controller.mobile;

import com.api.entity.Cliente;
import com.api.entity.Usuario;
import com.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cliente/perfil")
public class ClientePerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<?> obterPerfilLogado(Principal principal) {
        // 'principal.getName()' geralmente retorna o login (WhatsApp) autenticado via JWT
        String login = principal.getName();

        Usuario usuario = usuarioRepository.findByLogin(login).orElse(null);
        if (usuario == null || usuario.getCliente() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado para este usuário.");
        }

        Cliente cliente = usuario.getCliente();

        // Montamos um DTO ou Map com os dados que o app precisa exibir
        Map<String, Object> dadosPerfil = new HashMap<>();
        dadosPerfil.put("nome", cliente.getNome());
        dadosPerfil.put("whatsapp", cliente.getWhatsapp());
        dadosPerfil.put("email", cliente.getEmail());
        dadosPerfil.put("dataNascimento", cliente.getDataNascimento());


        dadosPerfil.put("cep", cliente.getEndereco().getCep());
        dadosPerfil.put("logradouro", cliente.getEndereco().getLogradouro());
        dadosPerfil.put("numero", cliente.getEndereco().getNumero());
        dadosPerfil.put("bairro", cliente.getEndereco().getBairro());
        dadosPerfil.put("cidade", cliente.getEndereco().getLocalidade());
        dadosPerfil.put("estado", cliente.getEndereco().getEstado());
        dadosPerfil.put("complemento", cliente.getEndereco().getComplemento());

        return ResponseEntity.ok(dadosPerfil);
    }
}
