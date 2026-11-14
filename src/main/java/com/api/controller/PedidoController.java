package com.api.controller; // Ajuste para o seu pacote correto

import com.api.apiExterno.mercadoPago.MercadoPagoService;
import com.api.business.PedidoService;
import com.api.dto.PedidoResponseDTO;
import com.api.dto.requests.PedidoRequest;
import com.api.eNum.FormaPagamento;
import com.api.eNum.StatusPedido;
import com.api.entity.Pedido;
import com.api.repository.PedidoRepository;
import jakarta.persistence.Table;
import org.apache.commons.lang.enums.Enum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @GetMapping("/listar")
    @Transactional(readOnly = true) // Use se for apenas consulta
    public List<PedidoResponseDTO> listarPedidosRecentes() {
        LocalDateTime doisDiasAtras = LocalDateTime.now().minusDays(2);
        return pedidoService.listarPedidosRecentes(doisDiasAtras);
    }

    @PostMapping("/criar")
    public ResponseEntity<?> criarPedido(@RequestBody PedidoRequest request) {
        return pedidoService.criarPedidoPDV(request);
    }

    @PutMapping("/{id}/etapa")
    @Transactional
    public ResponseEntity<Pedido> atualizarEtapa(@PathVariable Long id, @RequestParam StatusPedido etapa, @RequestParam(required = false) FormaPagamento formaPagamento) {

         return pedidoRepository.findById(id).map(pedido -> {
             var etapaAnterior = pedido.getEtapa();
             if(etapaAnterior.equals(StatusPedido.FINALIZADO)){
                 return ResponseEntity.ok(pedido);
             }

            pedido.setEtapa(etapa);
            if (etapa.equals(StatusPedido.PAGO) && formaPagamento != null && etapaAnterior.equals(StatusPedido.AGUARDANDO_PAGAMENTO)){
                pedido.setFormaPagamento(formaPagamento);
                pedidoService.efetuarVendaReservado(pedido);

            }

            if (pedido.getEtapa().equals(StatusPedido.CANCELADO) && !etapaAnterior.equals(StatusPedido.FINALIZADO)){
                pedidoService.cancelarPedido(pedido);
            }

            return ResponseEntity.ok(pedidoRepository.save(pedido));
        }).orElse(ResponseEntity.notFound().build());
    }
}