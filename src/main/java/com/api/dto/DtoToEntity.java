package com.api.dto;

import com.api.entity.*;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.ProdutoRepository;
import jakarta.persistence.Access;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DtoToEntity {

    @Autowired
    ProdutoFornecedorRepository produtoFornecedorRepository;

    public Venda vendaDtoToEntity(VendaDTO vendaDTO, List<ItemVenda> itemVendaList){

        Venda venda = new Venda();
        venda.setSubtotal(vendaDTO.getSubtotal());
        venda.setDesconto(vendaDTO.getDesconto());
        venda.setTotal(vendaDTO.getTotalFinal());
        venda.setCanalVenda(vendaDTO.getCanalVenda());
        venda.setStatusPedido(vendaDTO.getStatusPedido());
        List<PagamentoVenda> pagamentos = vendaDTO.getPagamentos().stream().map(pDto -> {
            PagamentoVenda pv = new PagamentoVenda();
            pv.setTipo(pDto.getTipo());
            pv.setValor(pDto.getValor());
            pv.setVenda(venda);
            return pv;
        }).collect(Collectors.toList());

        venda.setPagamentos(pagamentos);

        venda.setItens(itemVendaList);

        return venda;

    }

    public List<ItemVenda> itemVendaDtoToEntity (List<ItemVendaDTO> itemVendaDTO){
        List<ItemVenda> response = new ArrayList<>();

        for (ItemVendaDTO dto : itemVendaDTO) {

            Produto produto = produtoFornecedorRepository.getProdutoFornecedor(dto.getProdutoId()).getProduto();

            BigDecimal preco = dto.getPrecoUnitario();
            BigDecimal quantidade = dto.getQuantidade();

            BigDecimal subtotal = preco.multiply(quantidade)
                    .setScale(2, RoundingMode.HALF_UP);

            ItemVenda item = new ItemVenda();
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(preco);
            item.setSubtotal(subtotal);

            response.add(item);
        }

        return response;

    }

}
