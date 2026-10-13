package com.api.controller;

import com.api.eNum.StatusPedido;
import com.api.entity.Pedido;
import com.api.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody Pedido pedido) {
        if (pedido.getEtapa() == null) {
            pedido.setEtapa(StatusPedido.CRIADO);
        }
        Pedido novoPedido = pedidoRepository.save(pedido);
        return ResponseEntity.ok(novoPedido);
    }

    @PutMapping("/{id}/etapa")
    public ResponseEntity<Pedido> atualizarEtapa(@PathVariable Long id, @RequestParam StatusPedido etapa) {
        return pedidoRepository.findById(id).map(pedido -> {
            pedido.setEtapa(etapa);
            Pedido atualizado = pedidoRepository.save(pedido);
            return ResponseEntity.ok(atualizado);
        }).orElse(ResponseEntity.notFound().build());
    }
}
