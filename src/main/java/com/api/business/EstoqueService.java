package com.api.business;

import com.api.dto.DtoToEntity;
import com.api.dto.ItemVendaDTO;
import com.api.dto.MovimentacaoDTO;
import com.api.dto.VendaDTO;
import com.api.eNum.UnidadeMedida;
import com.api.entity.*;
import com.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.api.eNum.TipoMovimentacao.AJUSTE;
import static com.api.eNum.TipoMovimentacao.ENTRADA;

@Service
public class EstoqueService {

    @Autowired
    private VendaRepository vendaRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    DtoToEntity dtoToEntity;

    @Autowired
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Transactional
    public void processarMovimentacao(MovimentacaoDTO dto) {

        ProdutoFornecedor produtoFornecedorExistente = produtoFornecedorRepository.getProdutoFornecedor(dto.getProdutoId());

        Fornecedor fornecedor = fornecedorRepository.findById(dto.getFornecedorId())
                .orElseThrow(() -> new RuntimeException("FORNECEDOR NÃO ENCONTRADO"));

        if (produtoFornecedorExistente.getFornecedor() == null) {
            produtoFornecedorExistente.setFornecedor(fornecedor);
        }

        // Se for SAÍDA, checa se tem saldo antes de deixar tirar
        if (dto.getTipo() != ENTRADA && dto.getTipo() != AJUSTE) {

            if (Boolean.TRUE.equals(dto.getIsGramas())) {
                var quantidadeConvertida = dto.getQuantidade().divide(BigDecimal.valueOf(1000));
                dto.setQuantidade(quantidadeConvertida);
            }

            var qdtdAtual = produtoFornecedorExistente.getQuantidade();
            var retirada = dto.getQuantidade();

            if (qdtdAtual.compareTo(retirada) >= 0) {
                qdtdAtual = qdtdAtual.subtract(retirada);
                produtoFornecedorExistente.setQuantidade(qdtdAtual);
                if (qdtdAtual.compareTo(BigDecimal.ZERO) == 0) {
                    produtoFornecedorExistente.setProdutoAtivo(false);
                }

                produtoFornecedorRepository.save(produtoFornecedorExistente);

                dto.setDataValidade(null);
                dto.setPrecoCompra(null);
                dto.setPrecoVenda(null);

                registrarHistorico(dto, produtoFornecedorExistente, fornecedor);

                return;
            } else {
                throw new RuntimeException("SALDO ATUAL INSUFICIENTE");
            }
        }

        if (dto.getTipo() == AJUSTE) {
            produtoFornecedorExistente.setFornecedor(fornecedor);
            if (dto.getQuantidade().compareTo(BigDecimal.ONE) == 0) {
                produtoFornecedorExistente.setQuantidade(dto.getQuantidade());
            }
            if (dto.getDataValidade() != null) {
                produtoFornecedorExistente.setDataValidade(dto.getDataValidade());
            }
        }

        produtoFornecedorExistente.setPrecoVenda(dto.getPrecoVenda());
        produtoFornecedorExistente.setDataEntrada(LocalDateTime.now());
        produtoFornecedorExistente.setPrecoCompra(dto.getPrecoCompra());
        produtoFornecedorExistente.setQuantidade(produtoFornecedorExistente.getQuantidade().add(dto.getQuantidade()));

        if (dto.getFornecedorId() == null) {
            produtoFornecedorExistente.setFornecedor(fornecedor);
        }

        produtoFornecedorExistente.setProdutoAtivo(true);

        produtoFornecedorRepository.save(produtoFornecedorExistente);

        registrarHistorico(dto, produtoFornecedorExistente, fornecedor);
    }


    @Transactional
    public void processarNovaEntrada(MovimentacaoDTO dto) {
        // 1. Recuperamos o Produto navegando pela tabela de estoque
        // Como você garantiu que o vínculo sempre existe, o orElseThrow aqui
        // vira uma proteção contra inconsistências graves de banco.
        Produto produtoMaster = produtoFornecedorRepository.getProdutoFornecedor(dto.getProdutoId()).getProduto();

        // 2. Buscamos o Fornecedor da nota que está entrando
        Fornecedor fornecedor = fornecedorRepository.findById(dto.getFornecedorId())
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado ID: " + dto.getFornecedorId()));

        // 3. Criamos o NOVO LOTE (Isolamento de validade e custo)
        ProdutoFornecedor novoProdutoFornecedor = new ProdutoFornecedor();
        novoProdutoFornecedor.setProduto(produtoMaster);
        novoProdutoFornecedor.setFornecedor(fornecedor);

        // Dados da carga atual
        novoProdutoFornecedor.setQuantidade(dto.getQuantidade());
        novoProdutoFornecedor.setDataEntrada(LocalDateTime.now());
        novoProdutoFornecedor.setDataValidade(dto.getDataValidade());
        novoProdutoFornecedor.setPrecoCompra(dto.getPrecoCompra());
        novoProdutoFornecedor.setPrecoVenda(dto.getPrecoVenda());
        novoProdutoFornecedor.setUnidade(dto.getUnidadeMedida());
        novoProdutoFornecedor.setProdutoAtivo(true);

        // 4. Persistência e Histórico
        ProdutoFornecedor pfSalvo = produtoFornecedorRepository.save(novoProdutoFornecedor);
        registrarHistorico(dto, pfSalvo, fornecedor);
    }


    @Transactional
    public Venda processarNovaVenda(VendaDTO vendaDTO) {
        List<ItemVenda> itemVendaList = dtoToEntity.itemVendaDtoToEntity(vendaDTO.getItens());
        Venda venda = dtoToEntity.vendaDtoToEntity(vendaDTO, itemVendaList);

        venda.setItens(new ArrayList<>());

        for (ItemVendaDTO itemDto : vendaDTO.getItens()) {

            List<ProdutoFornecedor> lotes = produtoFornecedorRepository.findAtivosPorProduto(itemDto.getProdutoId());

            BigDecimal quantidadeRestante = itemDto.getQuantidade();


            BigDecimal estoqueTotal = lotes.stream()
                    .map(ProdutoFornecedor::getQuantidade)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (estoqueTotal.compareTo(quantidadeRestante) < 0) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + itemDto.getNome());
            }


            for (ProdutoFornecedor pf : lotes) {
                if (quantidadeRestante.compareTo(BigDecimal.ZERO) <= 0) break;

                BigDecimal estoqueAtualNoLote = pf.getQuantidade();
                BigDecimal quantidadeAbatidaDesteLote;

                if (estoqueAtualNoLote.compareTo(quantidadeRestante) >= 0) {
                    pf.setQuantidade(estoqueAtualNoLote.subtract(quantidadeRestante));
                    quantidadeAbatidaDesteLote = quantidadeRestante;
                    quantidadeRestante = BigDecimal.ZERO;
                } else {
                    quantidadeAbatidaDesteLote = estoqueAtualNoLote;
                    quantidadeRestante = quantidadeRestante.subtract(estoqueAtualNoLote);
                    pf.setQuantidade(BigDecimal.ZERO);
                }

                ItemVenda item = new ItemVenda();
                item.setVenda(venda);
                item.setProduto(pf.getProduto());
                item.setProdutoFornecedorId(pf); // Rastreabilidade do lote
                item.setQuantidade(quantidadeAbatidaDesteLote);
                item.setPrecoUnitario(pf.getPrecoVenda());
                item.setSubtotal(pf.getPrecoVenda().multiply(quantidadeAbatidaDesteLote));

                venda.getItens().add(item);

                return vendaRepository.save(venda);
            }
        }

        return venda;
    }


        //REGISTRAR NA TABELA DE HISTORICO.
        private void registrarHistorico (MovimentacaoDTO dto, ProdutoFornecedor pfSalvo, Fornecedor fornecedor){

            // 1. Tratamento de Unidade (Gramas para KG ou vice-versa)
            if (Boolean.TRUE.equals(dto.getIsGramas())) {
                dto.setUnidadeMedida(UnidadeMedida.GRAMAS.toString());
                // Se o seu padrão de banco for gramas, o cálculo está correto
                dto.setQuantidade(dto.getQuantidade().multiply(BigDecimal.valueOf(1000)));
            }

            MovimentacaoEstoque mov = new MovimentacaoEstoque();

            // 2. O PONTO CHAVE: Vincular ao Lote Específico
            // Certifique-se de que sua entidade MovimentacaoEstoque tenha esse campo
            mov.setProdutoFornecedor(pfSalvo);

            // Mantemos o Produto para consultas rápidas se necessário
            mov.setProduto(pfSalvo.getProduto());

            mov.setQuantidade(dto.getQuantidade());
            mov.setTipoMovimentacao(dto.getTipo());

            // 3. Snapshot de Preços (Auditoria)
            // Salvamos os valores que vieram no DTO, independente se o
            // ProdutoFornecedor for editado no futuro.
            mov.setPrecoCompra(dto.getPrecoCompra());
            mov.setPrecoVenda(dto.getPrecoVenda());
            mov.setUnidadeMedida(dto.getUnidadeMedida());
            mov.setData(LocalDateTime.now());
            mov.setDataValidade(dto.getDataValidade());

            if (fornecedor != null) {
                mov.setFornecedor(fornecedor);
            }

            movimentacaoRepository.save(mov);
        }
    }