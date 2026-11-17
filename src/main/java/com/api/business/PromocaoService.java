package com.api.business;

import com.api.dto.ItemKitDTO;
import com.api.entity.ProdutoFornecedor;
import com.api.entity.Promocao;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.ProdutoRepository;
import com.api.repository.PromocaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
public class PromocaoService {

    @Autowired
    private PromocaoRepository kitRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Transactional
    public Promocao criarPromocao(Promocao dto) {
        // 1. Gera o código único P + 13 dígitos
        String codigoUnico = "PROMO" + String.format("%05d", Math.abs(new Random().nextLong() % 10000L));
        dto.setCodigoPromocao(codigoUnico);
        dto.setAtivo(true);



        // 2. Valida e baixa o estoque dos itens guardados no JSON
        for (ItemKitDTO item : dto.getItens()) {
            ProdutoFornecedor produtoFornecedor = produtoFornecedorRepository.getProdutoFornecedor(item.getProdutoId());

            BigDecimal qtdNecessaria = item.getQuantidade().multiply(new BigDecimal(dto.getQuantidadeCombos()));

            // Se o estoque (retorno < 0) for menor que a quantidade necessária, lança o erro
            if (produtoFornecedor.getQuantidade().compareTo(qtdNecessaria) < 0) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produtoFornecedor.getNomeProduto());
            }

            // Preenche o nome opcionalmente para facilitar a leitura visual no banco
            item.setNomeProduto(produtoFornecedor.getProduto().getNome());
            item.setProdutoFormecedorId(produtoFornecedor.getId());

            // Baixa no estoque normal
            BigDecimal estoqueAtual = produtoFornecedor.getQuantidade();

            estoqueAtual = estoqueAtual.subtract(qtdNecessaria);
            produtoFornecedor.setQuantidade(estoqueAtual);
            produtoFornecedorRepository.save(produtoFornecedor);
        }

        return kitRepository.save(dto);
    }

    public List<Promocao> listarPromocoes(){
        return kitRepository.findByOcultarFalse();
    }

    @Transactional
    public void cancelarPromocao(Long kitId, Long qtdParaCancelar) {
        Promocao kit = kitRepository.findById(kitId)
                .orElseThrow(() -> new RuntimeException("Promoção não encontrada."));

        if (!kit.isAtivo()) {
            throw new RuntimeException("Esta promoção já está totalmente inativa/cancelada.");
        }

        if (qtdParaCancelar == null || qtdParaCancelar <= 0) {
            throw new RuntimeException("Informe uma quantidade válida para cancelar.");
        }

        if (qtdParaCancelar > kit.getQuantidadeCombos()) {
            throw new RuntimeException("A quantidade a cancelar (" + qtdParaCancelar + ") é maior do que a quantidade ativa em estoque (" + kit.getQuantidadeCombos() + ").");
        }

        // Estorna proporcionalmente os itens de volta para o estoque original
        for (ItemKitDTO item : kit.getItens()) {
            ProdutoFornecedor produtoFornecedor = produtoFornecedorRepository.getProdutoFornecedor(item.getProdutoId());
            if (produtoFornecedor != null) {
                // Qtd a devolver = Quantidade unitária do item no kit * Quantidade de combos cancelados
                BigDecimal qtdEstorno = item.getQuantidade().multiply(new BigDecimal(qtdParaCancelar));

                produtoFornecedor.setQuantidade(produtoFornecedor.getQuantidade().add(qtdEstorno));
                produtoFornecedorRepository.save(produtoFornecedor);
            }
        }

        // Atualiza a quantidade de combos restantes
        long novaQtd = kit.getQuantidadeCombos() - qtdParaCancelar;
        kit.setQuantidadeCombos(novaQtd);

        // Se zerou os combos, desativa o kit definitivamente
        if (novaQtd == 0) {
            kit.setAtivo(false);
        }

        kitRepository.save(kit);
    }

    @Transactional
    public void excluirPromocao(Long kitId) {
        Promocao kit = kitRepository.findById(kitId)
                .orElseThrow(() -> new RuntimeException("Promoção não encontrada."));

        kit.setOcultar(true);

        kitRepository.save(kit);
    }

}
