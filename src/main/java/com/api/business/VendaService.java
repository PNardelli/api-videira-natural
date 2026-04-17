package com.api.business;

import com.api.dto.ItemVendaDTO;
import com.api.dto.ProdutoFornecedorDTO;
import com.api.dto.VendaDTO;
import com.api.dto.requests.ItemCarrinhoRequest;
import com.api.dto.requests.ProdutoRequest;
import com.api.entity.ItemVenda;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import com.api.entity.Venda;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.ProdutoRepository;
import com.api.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class VendaService {


    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Autowired
    private ProdutoFornecedorService produtoFornecedorService;

    @Transactional
    public Venda finalizarVenda(VendaDTO dto) {
        Venda venda = DtoToEntity(dto);
        vendaRepository.save(venda); // Salva a "capa" primeiro

        for (ItemVendaDTO itemDto : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDto.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            ProdutoFornecedor produtoFornecedor = produtoFornecedorRepository.getProdutoFornecedor(itemDto.getProdutoId());

            // CORREÇÃO: Se a quantidade em estoque for MENOR que a pedida, erro.
            if (produtoFornecedor.getQuantidade().compareTo(itemDto.getQuantidade()) < 0) {
                throw new RuntimeException("Estoque insuficiente para: " + produto.getNome());
            }

            // Baixa o estoque
            produtoFornecedor.setQuantidade(produtoFornecedor.getQuantidade().subtract(itemDto.getQuantidade()));

            ItemVenda item = new ItemVenda();
            item.setVenda(venda);
            item.setProduto(produto);
            item.setQuantidade(itemDto.getQuantidade().intValue());
            item.setPrecoUnitario(produtoFornecedor.getPrecoVenda());

            venda.getItens().add(item);
        }

        return vendaRepository.save(venda);
    }

    private Venda DtoToEntity(VendaDTO dto) {
        Venda venda = new Venda();
        venda.setSubtotal(dto.getSubtotal());
        venda.setDesconto(dto.getDesconto());
        venda.setTotal(dto.getTotalFinal());
        venda.setCanalVenda(dto.getCanalVenda());
        venda.setStatusPedido(dto.getStatusPedido());
        // Se tiver cliente, busque o objeto cliente e dê um venda.setCliente()
        return venda;
    }




    public List<ItemCarrinhoRequest> processarAdicao(String termo,
                                                     List<ItemCarrinhoRequest> carrinho,
                                                     Long codProduto,
                                                     Double peso) {

        ItemCarrinhoRequest itemParaAdicionar = new ItemCarrinhoRequest();

        // CASO A: BALANÇA (etiqueta)
        if (termo != null && termo.length() == 13 && termo.startsWith("2")) {

            ProdutoRequest p = produtoFornecedorService.recuperarProdutoGranel(termo);

            itemParaAdicionar.setProdutoId(p.getProdutoId());
            itemParaAdicionar.setNome(p.getNome());
            itemParaAdicionar.setPreco(p.getPrecoVenda());
            itemParaAdicionar.setUnidade("KG");
            itemParaAdicionar.setQuantidade(p.getEstoque()); // peso da etiqueta
        }
        // CASO B: FRONT (botão capturar peso ou sugestão)
        else {
            ProdutoFornecedor produtoFornecedor = produtoFornecedorRepository.getProdutoFornecedor(codProduto);
            if (produtoFornecedor == null) return carrinho;

            itemParaAdicionar.setProdutoId(produtoFornecedor.getProduto().getId());
            itemParaAdicionar.setNome(produtoFornecedor.getProduto().getNome());
            itemParaAdicionar.setPreco(produtoFornecedor.getPrecoVenda());
            itemParaAdicionar.setUnidade(produtoFornecedor.getUnidade().substring(0, 2));

            // 🔥 CORREÇÃO AQUI
            if (peso != null) {
                itemParaAdicionar.setQuantidade(BigDecimal.valueOf(peso));
            } else {
                itemParaAdicionar.setQuantidade(BigDecimal.ONE);
            }
        }

        // SUBTOTAL
        BigDecimal subtotal = itemParaAdicionar.getPreco().multiply(itemParaAdicionar.getQuantidade());
        itemParaAdicionar.setSubTotal(subtotal);

        // 🔵 REGRA:
        // UN → soma
        // KG → duplica (como você quer)

        if ("UN".equalsIgnoreCase(itemParaAdicionar.getUnidade())) {
            for (ItemCarrinhoRequest itemNoCarrinho : carrinho) {
                if (itemNoCarrinho.getProdutoId().equals(itemParaAdicionar.getProdutoId())) {

                    itemNoCarrinho.setQuantidade(
                            itemNoCarrinho.getQuantidade().add(BigDecimal.ONE)
                    );

                    itemNoCarrinho.setSubTotal(
                            itemNoCarrinho.getPreco().multiply(itemNoCarrinho.getQuantidade())
                    );

                    return carrinho;
                }
            }
        }
        // KG ou item novo → adiciona novo item
        carrinho.add(itemParaAdicionar);
        return carrinho;
    }

}