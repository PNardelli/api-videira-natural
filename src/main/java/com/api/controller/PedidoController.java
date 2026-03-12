//package com.api.controller;
//
//import com.api.business.PedidoService;
//import com.api.dto.ItemPedidoRequest;
//import com.api.entity.Pedido;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/pedidos")
//public class PedidoController {
//
//    @Autowired
//    private PedidoService pedidoService;
//
//    @PostMapping
//    public Pedido criar() {
//        return pedidoService.criarPedido();
//    }
//
//    @PostMapping("/{id}/itens")
//    public void adicionarItem(@PathVariable Long id,
//                              @RequestBody ItemPedidoRequest dto) {
//
//        pedidoService.adicionarItem(
//                id,
//                dto.getProdutoId(),
//                dto.getQuantidade()
//        );
//    }
//
//    @DeleteMapping("/itens/{itemId}")
//    public void removerItem(@PathVariable Long itemId) {
//        pedidoService.removerItem(itemId);
//    }
//
//    @PutMapping("/{id}/finalizar")
//    public void finalizar(@PathVariable Long id) {
//        pedidoService.finalizarPedido(id);
//    }
//
//    @PutMapping("/{id}/cancelar")
//    public void cancelar(@PathVariable Long id) {
//        pedidoService.cancelarPedido(id);
//    }
//}
