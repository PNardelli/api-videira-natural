package com.api.controller; // Ajuste para o seu pacote correto

import com.api.apiExterno.mercadoPago.MercadoPagoService;
import com.api.business.PedidoService;
import com.api.dto.requests.PedidoRequest;
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

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> criarPedido(@RequestBody PedidoRequest request) {
        return pedidoService.criarPedidoPDV(request);
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