package com.api.controller;

import com.api.business.ClienteService;
import com.api.dto.ClienteDTO;
import com.api.dto.ClientePdvDTO;
import com.api.entity.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*") // libera para o Vue local
public class ClienteController {

    @Autowired
    ClienteService clienteService;

    // Criar cliente
    @PostMapping
    public ResponseEntity<Cliente> criar(@RequestBody ClienteDTO clienteDTO) {
        Cliente salvo = clienteService.salvarCliente(clienteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // Listar todos
    @GetMapping("/buscar")
    public List<Cliente> buscar(@RequestParam(required = false) String termo) {
        // 👉 SEM termo = comportamento atual (Gestão)
        if (termo == null || termo.isBlank()) {
            return clienteService.listarTodos();
        }
        // 👉 COM termo = busca otimizada (PDV)
        return clienteService.buscarPorNomeOuWhatsapp(termo);
    }

    @GetMapping("/pdv")
    public List<ClientePdvDTO> buscarPdv(@RequestParam String termo) {
        return clienteService.buscarClientesPdv(termo);
    }

    // Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscar(@PathVariable Long id) {
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable Long id, @RequestBody Cliente clienteDTO){

        Cliente atualizado = clienteService.atualizarCliente(id, clienteDTO);
        return ResponseEntity.ok(atualizado);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(@PathVariable Long id) {
        clienteService.alterarStatus(id);
        return ResponseEntity.noContent().build();
    }

    // Deletar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

