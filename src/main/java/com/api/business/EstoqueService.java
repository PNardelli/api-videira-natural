package com.api.business;

import com.api.dto.MovimentacaoDTO;
import com.api.eNum.TipoMovimentacao;
import com.api.eNum.UnidadeMedida;
import com.api.entity.Fornecedor;
import com.api.entity.MovimentacaoEstoque;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import com.api.repository.FornecedorRepository;
import com.api.repository.MovimentacaoEstoqueRepository;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EstoqueService {

    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private ProdutoFornecedorRepository pfRepository;
    @Autowired
    private ProdutoFornecedorService pfService;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Transactional
    public void processarMovimentacao(MovimentacaoDTO dto) {

        ProdutoFornecedor produtoFornecedorExistente = pfRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RuntimeException("PRODUTO NÃO ENCONTRADO"));

        Fornecedor fornecedor = fornecedorRepository.findById(dto.getFornecedorId())
                .orElseThrow(() -> new RuntimeException("FORNECEDOR NÃO ENCONTRADO"));

        if (produtoFornecedorExistente.getFornecedor() == null){
            produtoFornecedorExistente.setFornecedor(fornecedor);
        }

        // Se for SAÍDA, checa se tem saldo antes de deixar tirar
        if (dto.getTipo() != TipoMovimentacao.ENTRADA) {

            if (Boolean.TRUE.equals(dto.getIsGramas())){
                var quantidadeConvertida = dto.getQuantidade().divide(BigDecimal.valueOf(1000));
                dto.setQuantidade(quantidadeConvertida);
            }

            var qdtdAtual = produtoFornecedorExistente.getQuantidade();
            var retirada = dto.getQuantidade();

            if (qdtdAtual.compareTo(retirada) >= 0) {
                qdtdAtual = qdtdAtual.subtract(retirada);
                produtoFornecedorExistente.setQuantidade(qdtdAtual);

                pfRepository.save(produtoFornecedorExistente);

                dto.setDataValidade(null);
                dto.setPrecoCompra(null);
                dto.setPrecoVenda(null);

                registrarHistorico(dto, produtoFornecedorExistente, fornecedor);

                return;
            } else {
                // Estoque insuficiente
                throw new RuntimeException("SALDO ATUAL INSUFICIENTE");
            }
        }

        produtoFornecedorExistente.setPrecoVenda(dto.getPrecoVenda());
        produtoFornecedorExistente.setDataEntrada(LocalDateTime.now());
        produtoFornecedorExistente.setPrecoCompra(dto.getPrecoCompra());
        produtoFornecedorExistente.setQuantidade(produtoFornecedorExistente.getQuantidade().add(dto.getQuantidade()));
        produtoFornecedorExistente.setDataValidade(dto.getDataValidade());

        if (dto.getFornecedorId() != null){
            produtoFornecedorExistente.setFornecedor(fornecedor);
        }

        pfRepository.save(produtoFornecedorExistente);

        registrarHistorico(dto, produtoFornecedorExistente, fornecedor);

    }


    @Transactional
    public void processarNovaEntrada(MovimentacaoDTO movimentacaoDTO) {
        Produto produto = produtoRepository.findById(movimentacaoDTO.getProdutoId())
                .orElseThrow(() -> new RuntimeException("PRODUTO NÃO ENCONTRADO"));

        Fornecedor fornecedor = fornecedorRepository.findById(movimentacaoDTO.getFornecedorId())
                .orElseThrow(() -> new RuntimeException("FORNECEDOR NÃO ENCONTRADO"));

        // 1. Tenta buscar o vínculo existente ou cria um novo "vazio"
        ProdutoFornecedor pf = pfRepository
                .findByProdutoAndFornecedor(produto, fornecedor)
                .orElseGet(() -> {
                    ProdutoFornecedor novo = new ProdutoFornecedor();
                    novo.setProduto(produto);
                    novo.setFornecedor(fornecedor);
                    novo.setQuantidade(BigDecimal.ZERO); // Começa com zero para somar depois
                    return novo;
                });

        // 2. Atualiza os dados (isso vale tanto para o NOVO quanto para o EXISTENTE)
        // Usando .add() assumindo que quantidade é BigDecimal
        pf.setQuantidade(pf.getQuantidade().add(movimentacaoDTO.getQuantidade()));

        pf.setDataEntrada(LocalDateTime.now());
        pf.setDataValidade(movimentacaoDTO.getDataValidade());
        pf.setPrecoCompra(movimentacaoDTO.getPrecoCompra());
        pf.setPrecoVenda(movimentacaoDTO.getPrecoVenda());
        pf.setUnidade(movimentacaoDTO.getUnidadeMedida());

        // 3. Salva uma única vez (O JPA resolve se é INSERT ou UPDATE pelo ID)
        ProdutoFornecedor pfSalvo = pfRepository.save(pf);

        // 4. Registra o histórico uma única vez ao final
        registrarHistorico(movimentacaoDTO, pfSalvo, fornecedor);
    }

    //REGISTRAR NA TABELA DE HISTORICO.
    private void registrarHistorico(MovimentacaoDTO dto, ProdutoFornecedor produtoFornecedor, Fornecedor fornecedor){

        if (Boolean.TRUE.equals(dto.getIsGramas())){
            dto.setUnidadeMedida(UnidadeMedida.GRAMAS.toString());
            var convertido = dto.getQuantidade().multiply(BigDecimal.valueOf(1000));
            dto.setQuantidade(convertido);
        }

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setProduto(produtoFornecedor.getProduto());
        mov.setQuantidade(dto.getQuantidade());
        mov.setTipoMovimentacao(dto.getTipo());
        mov.setPrecoCompra(dto.getPrecoCompra());
        mov.setPrecoVenda(dto.getPrecoVenda());
        mov.setUnidadeMedida(dto.getUnidadeMedida());
        if (dto.getTipo() == TipoMovimentacao.ENTRADA){
            mov.setObservacao(dto.getObservacao() == null ? dto.getObservacao().toUpperCase() : "ENTRADA DE ESTOQUE");
        }if (dto.getTipo() == TipoMovimentacao.SAIDA){
            mov.setObservacao(dto.getObservacao() == null ? dto.getObservacao().toUpperCase() : "SAIDA DE ESTOQUE");
        }if (dto.getTipo() == TipoMovimentacao.PERDA){
            mov.setObservacao(dto.getObservacao() == null ? dto.getObservacao().toUpperCase() : "PERCA DE PRODUTOS");
        }
        mov.setData(LocalDateTime.now());
        mov.setDataValidade(dto.getDataValidade());

        if (dto.getFornecedorId() != null) {
            mov.setFornecedor(fornecedor);
        }
        movimentacaoRepository.save(mov);
    }
}