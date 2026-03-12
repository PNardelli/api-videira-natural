//package com.api.business;
//
//import com.api.entity.ItemPedido;
//import com.api.entity.Pedido;
//import com.api.entity.Produto;
//import com.api.eNum.StatusPedido;
//import com.api.repository.ItemPedidoRepository;
//import com.api.repository.PedidoRepository;
//import com.api.repository.ProdutoRepository;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//public class PedidoService {
//
//    @Autowired
//    private PedidoRepository pedidoRepository;
//
//    @Autowired
//    private ProdutoRepository produtoRepository;
//
//    @Autowired
//    private ItemPedidoRepository itemPedidoRepository;
//
//    // 🛒 Criar carrinho
//    @Transactional
//    public Pedido criarPedido() {
//
//        Pedido pedido = new Pedido();
//        pedido.setData(LocalDateTime.now());
//        pedido.setStatus(StatusPedido.CRIADO);
//        pedido.setValorTotal(BigDecimal.ZERO);
//
//        return pedidoRepository.save(pedido);
//    }
//
//    // 🛒 Adicionar item ao carrinho
//    @Transactional
//    public void adicionarItem(Long pedidoId, Long produtoId, BigDecimal quantidade) {
//
//        Pedido pedido = buscarPedido(pedidoId);
//
//        if (pedido.getStatus() != StatusPedido.CRIADO)
//            throw new RuntimeException("Pedido não pode ser alterado");
//
//        Produto produto = buscarProduto(produtoId);
//
//        if (!produto.getAtivo())
//            throw new RuntimeException("Produto inativo");
//
//        if (produto.getEstoque().compareTo(quantidade) < 0)
//            throw new RuntimeException("Estoque insuficiente");
//
//        BigDecimal subtotal = produto.getPreco().multiply(quantidade);
//
//        ItemPedido item = new ItemPedido();
//        item.setPedido(pedido);
//        item.setProduto(produto);
//        item.setQuantidade(quantidade);
//        item.setPrecoUnitario(produto.getPreco());
//        item.setSubTotal(subtotal);
//
//        itemPedidoRepository.save(item);
//
//        // desconta estoque
//        produto.setEstoque(produto.getEstoque().subtract(quantidade));
//
//        recalcularTotal(pedido);
//    }
//
//    // 🛒 Remover item
//    @Transactional
//    public void removerItem(Long itemId) {
//
//        ItemPedido item = itemPedidoRepository.findById(itemId)
//                .orElseThrow(() -> new RuntimeException("Item não encontrado"));
//
//        Pedido pedido = item.getPedido();
//
//        if (pedido.getStatus() != StatusPedido.CRIADO)
//            throw new RuntimeException("Pedido não pode ser alterado");
//
//        // devolve estoque
//        Produto produto = item.getProduto();
//        produto.setEstoque(produto.getEstoque().add(item.getQuantidade()));
//
//        itemPedidoRepository.delete(item);
//
//        recalcularTotal(pedido);
//    }
//
//    // 🛒 Finalizar pedido
//    @Transactional
//    public void finalizarPedido(Long pedidoId) {
//
//        Pedido pedido = buscarPedido(pedidoId);
//
//        if (pedido.getValorTotal().compareTo(BigDecimal.ZERO) <= 0)
//            throw new RuntimeException("Pedido vazio");
//
//        pedido.setStatus(StatusPedido.PAGO);
//    }
//
//    // 🛒 Cancelar pedido
//    @Transactional
//    public void cancelarPedido(Long pedidoId) {
//
//        Pedido pedido = buscarPedido(pedidoId);
//
//        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);
//
//        for (ItemPedido item : itens) {
//            Produto produto = item.getProduto();
//            produto.setEstoque(produto.getEstoque().add(item.getQuantidade()));
//        }
//
//        pedido.setStatus(StatusPedido.CANCELADO);
//    }
//
//    private void recalcularTotal(Pedido pedido) {
//
//        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);
//
//        BigDecimal total = itens.stream()
//                .map(ItemPedido::getSubTotal)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        pedido.setValorTotal(total);
//    }
//
//    private Pedido buscarPedido(Long id) {
//        return pedidoRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
//    }
//
//    private Produto buscarProduto(Long id) {
//        return produtoRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
//    }
//}
