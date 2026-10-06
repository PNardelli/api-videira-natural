package com.api.business;

import com.api.dto.ProdutoFornecedorDTO;
import com.api.dto.VendaDTO;
import com.api.dto.requests.ItemCarrinhoRequest;
import com.api.dto.requests.ProdutoRequest;
import com.api.entity.*;
import com.api.repository.ClienteRepository;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.ProdutoRepository;
import com.api.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VendaService {

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Autowired
    private ProdutoFornecedorService produtoFornecedorService;

    @Transactional
    public Venda finalizarVenda(VendaDTO dto) {
        return estoqueService.processarNovaVenda(dto);
    }

    public List<ItemCarrinhoRequest> processarAdicao(String termo, Pageable pageable,
                                                     List<ItemCarrinhoRequest> carrinho,
                                                     Long codProduto,
                                                     Double peso) {

        ItemCarrinhoRequest itemParaAdicionar = new ItemCarrinhoRequest();

        // CASO A: BALANÇA (etiqueta)
        if (termo != null && termo.length() == 13 && termo.startsWith("2")) {

            ProdutoRequest p = produtoFornecedorService.recuperarProdutoGranel(termo, pageable);

            itemParaAdicionar.setProdutoId(p.getProdutoId());
            itemParaAdicionar.setNome(p.getNome());
            itemParaAdicionar.setPreco(p.getPrecoVenda());
            itemParaAdicionar.setUnidade("KG");
            itemParaAdicionar.setQuantidade(p.getEstoque()); // peso da etiqueta
        }else if (termo != null && termo.length() >= 13 && termo.charAt(0) != '2'){
            // CASO B: Produto Industrializado EAN.
            List<ProdutoFornecedorDTO> p = produtoService.buscarFlexivel(termo, pageable);

            itemParaAdicionar.setProdutoId(p.get(0).getProdutoId());
            itemParaAdicionar.setNome(p.get(0).getNome());
            itemParaAdicionar.setPreco(p.get(0).getPrecoVenda());
            itemParaAdicionar.setUnidade(p.get(0).getUnidade());
            itemParaAdicionar.setQuantidade(BigDecimal.ONE);

            BigDecimal estoqueConvertidoParaUnidade = p.get(0).getQuantidade();
            itemParaAdicionar.setEstoque(estoqueConvertidoParaUnidade.stripTrailingZeros().toString());
        }


        // CASO C: FRONT (botão capturar peso ou sugestão)
        else {
            ProdutoFornecedor produtoFornecedor = produtoFornecedorRepository.getProdutoFornecedorList(codProduto, PageRequest.of(0, 3))
                    .get(0);

            if (produtoFornecedor == null) return carrinho;

            itemParaAdicionar.setProdutoId(produtoFornecedor.getProduto().getId());
            itemParaAdicionar.setNome(produtoFornecedor.getProduto().getNome());
            itemParaAdicionar.setPreco(produtoFornecedor.getPrecoVenda());
            itemParaAdicionar.setUnidade(produtoFornecedor.getUnidadeMedida().substring(0, 2));
            itemParaAdicionar.setEstoque(produtoFornecedor.getQuantidade().toString());

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