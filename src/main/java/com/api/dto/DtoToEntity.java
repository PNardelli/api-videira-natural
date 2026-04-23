package com.api.dto;

import com.api.eNum.StatusPedido;
import com.api.eNum.TipoVenda;
import com.api.entity.*;
import com.api.repository.ProdutoFornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

    public Venda vendaDtoToEntity(VendaDTO vendaDTO, List<ItemVenda> itemVendaList, Cliente cliente){

        Venda venda = new Venda();
        venda.setSubtotal(vendaDTO.getSubtotal());
        venda.setDesconto(vendaDTO.getDesconto());
        venda.setTotal(vendaDTO.getTotalFinal());
        venda.setCanalVenda(vendaDTO.getCanalVenda());
        if (venda.getCanalVenda() == TipoVenda.PDV) {
            venda.setStatusPedido(StatusPedido.FINALIZADO);
        }
        List<PagamentoVenda> pagamentos = vendaDTO.getPagamentos().stream().map(pDto -> {
            PagamentoVenda pv = new PagamentoVenda();
            pv.setTipo(pDto.getTipo());
            pv.setValor(pDto.getValor());
            pv.setVenda(venda);
            return pv;
        }).collect(Collectors.toList());

        venda.setPagamentos(pagamentos);

        venda.setItens(itemVendaList);

        if (cliente != null){
            int coinsGeradas = venda.getTotal().intValue();
            venda.setCoinsGeradas(coinsGeradas);


            int saldoAtual = cliente.getVideiraSaldo();
            int saldoAtualizado = saldoAtual + coinsGeradas;
            cliente.setVideiraSaldo(saldoAtualizado);
            venda.setCliente(cliente);
        }

        return venda;

    }

    public List<ItemVenda> itemVendaDtoToEntity (List<ItemVendaDTO> itemVendaDTO){
        List<ItemVenda> response = new ArrayList<>();

        for (ItemVendaDTO dto : itemVendaDTO) {

            List<ProdutoFornecedor> produtoFornecedorList = produtoFornecedorRepository.getProdutoFornecedorList(dto.getProdutoId(), PageRequest.of(0,3));

            BigDecimal preco = dto.getPrecoUnitario();
            BigDecimal quantidade = dto.getQuantidade();

            BigDecimal subtotal = preco.multiply(quantidade)
                    .setScale(2, RoundingMode.HALF_UP);


            ItemVenda item = new ItemVenda();
            item.setProduto(produtoFornecedorList.get(0).getProduto());
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(preco);
            item.setSubtotal(subtotal);

            response.add(item);
        }

        return response;

    }

}
