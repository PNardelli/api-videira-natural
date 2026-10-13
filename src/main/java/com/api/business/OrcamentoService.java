//package com.api.business;
//
//import com.api.dto.ItemVendaDTO;
//import com.api.dto.VendaDTO;
//import com.api.entity.Orcamento;
//import com.api.entity.OrcamentoItemEmbeddable;
//import com.api.entity.Produto;
//import com.api.repository.OrcamentoRepository;
//import com.api.repository.ProdutoRepository;
//import com.api.uteis.Util;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class OrcamentoService {
//
//    @Autowired
//    private Util util;
//
//    @Autowired
//    private final OrcamentoRepository repository;
//
//    @Autowired
//    private ProdutoRepository produtoRepository;
//
//
//    @Transactional
//    public String salvarOrcamento(VendaDTO dto) {
//        Orcamento orcamento = new Orcamento();
//        orcamento.setCodigoRecuperacao(util.GeradorCodigo()); // Ex: "739201"
//
//        dto.getItens().forEach(item -> {
//            OrcamentoItemEmbeddable oItem = new OrcamentoItemEmbeddable();
//            oItem.setProdutoId(item.getProdutoId());
//            oItem.setQuantidade(item.getQuantidade());
//            orcamento.getItens().add(oItem);
//        });
//
//        repository.save(orcamento);
//        return orcamento.getCodigoRecuperacao();
//    }
//
//    @Transactional(readOnly = true)
//    public VendaDTO recuperar(String codigo) {
//        Orcamento orcamento = repository.findByCodigoRecuperacao(codigo)
//                .orElseThrow(() -> new EntityNotFoundException("Orçamento expirado ou inexistente"));
//
//        // Aqui você mapeia de volta para o DTO que o Vue entende
//        return mapToDTO(orcamento);
//    }
//
//
//    private VendaDTO mapToDTO(Orcamento orcamento) {
//        VendaDTO dto = new VendaDTO();
//
//        // Se o orçamento tiver um cliente vinculado, levamos o ID
//        if (orcamento.getCliente() != null) {
//            dto.setClienteId(orcamento.getCliente().getId());
//        }
//
//        List<ItemVendaDTO> itensDTO = orcamento.getItens().stream().map(item -> {
//            // Buscamos o produto para garantir que o Nome e Preço estão atualizados
//            Produto p = produtoRepository.findById(item.getProdutoId())
//                    .orElseThrow(() -> new RuntimeException("Produto ID " + item.getProdutoId() + " não existe mais."));
//
//            ItemVendaDTO itemDto = new ItemVendaDTO();
//            itemDto.setProdutoId(p.getId());
//            itemDto.setNome(p.getNome()); // Adicionamos o nome para o Vue exibir na tabela
//            itemDto.setPrecoUnitario(p.getPrecoVenda()); // Preço atual do balcão
//            itemDto.setQuantidade(item.getQuantidade());
//
//            return itemDto;
//        }).collect(Collectors.toList());
//
//        dto.setItens(itensDTO);
//
//        // Recalculamos o subtotal com base nos preços atuais
//        BigDecimal subtotal = itensDTO.stream()
//                .map(i -> i.getPrecoUnitario().multiply(new BigDecimal(String.valueOf(i.getQuantidade()))))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        dto.setSubtotal(subtotal);
//        dto.setTotalFinal(subtotal); // O desconto o vendedor aplica de novo se quiser
//
//        return dto;
//    }
//
//
//}
