package com.api.controller.mobile;

import com.api.apiExterno.mercadoPago.MercadoPagoService;
import com.api.business.PedidoService;
import com.api.dto.requests.ItemCarrinhoRequest;
import com.api.dto.requests.PedidoRequest;
import com.api.eNum.FormaPagamento;
import com.api.eNum.StatusPedido;
import com.api.eNum.TipoVenda;
import com.api.entity.Pedido;
import com.api.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pedidos/online")
@CrossOrigin(origins = "*")
public class PedidoOnlineController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @PostMapping("/criar")
    @Transactional
    @CacheEvict(value = "vitrineCache", allEntries = true)
    public ResponseEntity<?> criarPedidoOnline(@RequestBody PedidoRequest request) {
        try {
        Pedido pedidoCriado = pedidoService.criarPedidoInterno(request);

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("idVendaReal", pedidoCriado.getId());

            // 2. Se a forma de pagamento for online (Cartão via Mercado Pago), gera a preferência
            if ("CARTAO_CREDITO".equalsIgnoreCase(request.getFormaPagamento())) {
                String initPoint = mercadoPagoService.criarPreferenciaCheckout(pedidoCriado, request.getItens());
                resposta.put("initPoint", initPoint);
                resposta.put("urlPagamento", initPoint);
            }
            // Se for Pix, você pode retornar os dados do Pix Copia e Cola aqui se desejar

            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            System.err.println("Erro ao criar pedido online: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "Erro ao processar pedido: " + e.getMessage()));
        }
    }
}
