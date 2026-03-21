package com.api.business;

import com.api.dto.MovimentacaoDTO;
import com.api.eNum.TipoMovimentacao;
import com.api.entity.Fornecedor;
import com.api.entity.MovimentacaoEstoque;
import com.api.entity.Produto;
import com.api.repository.FornecedorRepository;
import com.api.repository.MovimentacaoEstoqueRepository;
import com.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EstoqueService {

    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Transactional
    public void processarMovimentacao(MovimentacaoDTO dto) {
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RuntimeException("PRODUTO NÃO ENCONTRADO"));
        // Aqui você chama o seu service que:
        // 1. Busca o produto OK
        // 2. Busca o fornecedor OK
        // 3. Salva a MovimentacaoEstoque OK
        // 4. Atualiza o saldo de estoque se necessário


        // Se for SAÍDA, checa se tem saldo antes de deixar tirar
        if (dto.getTipo() != TipoMovimentacao.ENTRADA) {
            Integer saldo = movimentacaoRepository.getSaldoAtual(dto.getProdutoId());
            int atual = (saldo != null) ? saldo : 0;
            if (atual < dto.getQuantidade()) throw new RuntimeException("ESTOQUE INSUFICIENTE! SALDO: " + atual);
        }

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setProduto(produto);
        mov.setQuantidade(dto.getQuantidade());
        mov.setTipoMovimentacao(dto.getTipo());
        mov.setObservacao(dto.getObservacao() != null ? dto.getObservacao().toUpperCase() : "");
        mov.setData(LocalDateTime.now());
        mov.setDataValidade(dto.getDataValidade());

        if (dto.getFornecedorId() != null) {
            mov.setFornecedor(fornecedorRepository.findById(dto.getFornecedorId()).orElse(null));
        }

        movimentacaoRepository.save(mov);
    }
}