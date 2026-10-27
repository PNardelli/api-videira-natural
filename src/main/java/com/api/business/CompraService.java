package com.api.business;

import com.api.dto.CompraDTO;
import com.api.dto.CompraDetalheDTO;
import com.api.dto.CompraResumoDTO;
import com.api.dto.ItemCompraDTO;
import com.api.entity.*;
import com.api.repository.CompraRepository;
import com.api.repository.EstoqueRepository;
import com.api.repository.FornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private ProdutoService produtoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Transactional
    public Compra salvarCompra(CompraDTO dto) {
        Fornecedor fornecedor = fornecedorRepository.findById(dto.getFornecedorId())
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));

        Compra compra = new Compra();
        compra.setFornecedor(fornecedor);

        List<ItemCompra> itens = new ArrayList<>();
        for (ItemCompraDTO itemDto : dto.getItens()) {
            List<ProdutoFornecedor> produtoFornecedor = estoqueRepository.recuperarProdutoFornecedorLista(itemDto.getProdutoId(), fornecedor.getId());

            ItemCompra item = new ItemCompra();
            item.setCompra(compra);
            item.setProdutoFornecedor(produtoFornecedor.get(0));
            item.setQuantidade(itemDto.getQuantidade());
            item.setValorUnitario(produtoFornecedor.get(0).getPrecoCompra() != null ? produtoFornecedor.get(0).getPrecoCompra().doubleValue() : 0.0);
            item.setValorVenda(produtoFornecedor.get(0).getPrecoVenda().doubleValue());
            item.setProdutoId(produtoFornecedor.get(0).getProduto().getId());
            itens.add(item);
        }

        compra.setItens(itens);
        return compraRepository.save(compra);
    }

    @Transactional
    public Compra editarCompraPorId(Long id, CompraDTO compraAlterada) {
        // 1. Busca a compra existente no banco com seus itens atuais
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada."));

        // Mapeia os itens atuais que já estão no banco por ID para facilitar a localização
        Map<Long, ItemCompra> itensAtuaisMap = compra.getItens().stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ItemCompra::getId, item -> item));

        List<ItemCompra> novaListaItens = new ArrayList<>();

        if (compraAlterada.getItens() != null) {
            for (var itemDto : compraAlterada.getItens()) {
                ItemCompra item;

                // Se o DTO trouxer um ID e ele já existir no banco, aproveitamos o registro (mantém o ID antigo!)
                if (itemDto.getId() != null && itensAtuaisMap.containsKey(itemDto.getId())) {
                    item = itensAtuaisMap.get(itemDto.getId());
                } else {
                    // Se for um item novo adicionado na tela, criamos um novo objeto
                    item = new ItemCompra();
                    item.setCompra(compra);
                }

                // Atualiza os valores direto no item (existente ou novo)
                item.setQuantidade(itemDto.getQuantidade());
                item.setValorUnitario(itemDto.getValorUnitario());
                item.setValorVenda(itemDto.getValorVenda());

                novaListaItens.add(item);
            }
        }

        // 2. Atualiza a lista da compra (o JPA vai atualizar os modificados, inserir os novos e remover os que sumiram da lista)
        compra.getItens().clear();
        compra.getItens().addAll(novaListaItens);

        // 3. Salva e retorna
        return compraRepository.save(compra);
    }

    @Transactional(readOnly = true) // <-- Garante sessão aberta para a projeção do DTO
    public List<CompraResumoDTO> listarResumo() {
        return compraRepository.listarResumoCompras();
    }

    @Transactional(readOnly = true)
    public CompraDetalheDTO buscarDetalhesPorId(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));

        // Converte os itens da entidade para a lista de ItemCompraDTO
        List<ItemCompraDTO> itensDto = compra.getItens().stream().map(item ->
                new ItemCompraDTO(
                        item.getId(),// id
                        item.getProdutoFornecedor().getProduto().getId() != null ? item.getProdutoFornecedor().getProduto().getId() : null,   // produtoId
                        item.getProdutoFornecedor().getProduto() != null ? item.getProdutoFornecedor().getProduto().getNome() : "Produto", // nomeProduto
                        item.getProdutoFornecedor().getProduto() != null ? item.getProdutoFornecedor().getProduto().getUnidadeMedida().toString() : "UN", // unidadeMedida
                        item.getQuantidade(),                                           // quantidade
                        item.getValorUnitario(),
                        item.getValorUnitario(),
                        item.getValorVenda()

                )
        ).collect(Collectors.toList());

        // Retorna o DTO de detalhes completo preenchendo o cabeçalho e os itens
        return new CompraDetalheDTO(
                compra.getId(),
                compra.getDataHora(),
                compra.getStatus(),
                compra.getPrevisaoEntrega(),
                compra.getDataEntrega(),
                compra.getFornecedor() != null ? compra.getFornecedor().getId() : null,
                compra.getFornecedor() != null ? compra.getFornecedor().getNome() : "Não informado",
                itensDto
        );
    }


    @Transactional
    public void deletarItemCompra(Long id, Long idCompra) {
        compraRepository.excluirItemDaCompra(id, idCompra);
    }

    @Transactional
    public Compra ajustarPrevisao(@PathVariable Long id, LocalDate previsao) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));
        compra.setPrevisaoEntrega(previsao);
        compra.setStatus("AGUARDANDO");
        return compraRepository.save(compra);
    }

    @Transactional
    public void processarRecebimentoEstoque(Long id, String numeroNotaFiscal) throws Exception {
        try{

            Compra compra = compraRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Compra não encontrada"));

            if ("RECEBIDA".equals(compra.getStatus())) {
                throw new RuntimeException("Esta compra já foi recebida.");
            }

            // Percorre os itens e chama o método que está no outro Service
            for (ItemCompra item : compra.getItens()) {
                if (item.getProdutoFornecedor().getProduto().getId() != null) {
                    estoqueService.novaEntradaViaCompras(item);
                }
            }
            compra.setStatus("RECEBIDA");
            compra.setIdentificadorNota(numeroNotaFiscal);
            compra.setDataEntrega(LocalDate.now());

            compraRepository.save(compra);

        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}