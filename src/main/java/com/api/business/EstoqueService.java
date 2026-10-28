package com.api.business;

import com.api.dto.DtoToEntity;
import com.api.dto.ItemVendaDTO;
import com.api.dto.MovimentacaoDTO;
import com.api.dto.VendaDTO;
import com.api.eNum.UnidadeMedida;
import com.api.entity.*;
import com.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.apache.velocity.runtime.directive.contrib.For;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.api.eNum.TipoMovimentacao.*;
import static java.util.Arrays.stream;

@Service
public class EstoqueService {

    @Autowired
    private VendaRepository vendaRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    DtoToEntity dtoToEntity;

    @Autowired
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Transactional
    public void processarMovimentacao(MovimentacaoDTO dto) {

        ProdutoFornecedor produtoFornecedorExistente = estoqueRepository.findById(dto.getIdVinculo()).orElse(null);

        if (!Objects.equals(dto.getFornecedorId(), dto.getFornecedorIdAntigo())) {
            Fornecedor fornecedor = fornecedorRepository.findById(dto.getFornecedorId())
                    .orElseThrow(() -> new RuntimeException("FORNECEDOR NÃO ENCONTRADO"));
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

                registrarHistorico(dto, produtoFornecedorExistente, produtoFornecedorExistente.getFornecedor());

                return;
            } else {
                throw new RuntimeException("INFO - SALDO ATUAL INSUFICIENTE");
            }
        }

        if (dto.getTipo() == AJUSTE) {
            produtoFornecedorExistente.setQuantidade(dto.getQuantidade());
            if (dto.getDataValidade() != null) {
                produtoFornecedorExistente.setDataValidade(dto.getDataValidade());
            }
        }

        if (dto.getTipo() == ENTRADA){
            assert produtoFornecedorExistente != null;
            produtoFornecedorExistente.setQuantidade(produtoFornecedorExistente.getQuantidade()
                    .add(dto.getQuantidade()));
        }
        produtoFornecedorExistente.setEstoqueMinimo(dto.getEstoqueMinimo());
        produtoFornecedorExistente.setPrecoVenda(dto.getPrecoVenda());
        produtoFornecedorExistente.setDataEntrada(LocalDateTime.now());
        produtoFornecedorExistente.setPrecoCompra(dto.getPrecoCompra());

        if (dto.getDataValidade() != null){
            produtoFornecedorExistente.setDataValidade(dto.getDataValidade());
        }

        produtoFornecedorExistente.setProdutoAtivo(true);

        produtoFornecedorRepository.save(produtoFornecedorExistente);

        produtoService.atualizarPreco(produtoFornecedorExistente.getProduto().getId(), dto.getPrecoVenda());
        desativarProdutoFornecedor(null, produtoFornecedorExistente.getProduto().getId());

        registrarHistorico(dto, produtoFornecedorExistente, produtoFornecedorExistente.getFornecedor());
    }


    @Transactional
    public void processarNovaEntrada(MovimentacaoDTO dto) {

        ProdutoFornecedor loteEncontrado = null;
        List<ProdutoFornecedor> loteProdutoFornecedor = estoqueRepository.recuperarProdutoFornecedorLista(dto.getProdutoId(), dto.getFornecedorId());

        if(!loteProdutoFornecedor.isEmpty()){
        loteEncontrado = loteProdutoFornecedor.stream()
                .filter(pf -> pf.getProdutoAtivo() != null && !pf.getProdutoAtivo())
                .findFirst()
                .orElse(null);
        }
        //Se for nulo ele cria um do zero, procurando um Produto Refencia e o Fornecedor
        if (loteEncontrado == null) {

            loteProdutoFornecedor = List.of(new ProdutoFornecedor());

            Produto produtoMaster = produtoService.buscar(dto.getProdutoId());
            Fornecedor fornecedorMaster = fornecedorRepository.findById(dto.getFornecedorId()).orElseThrow();

                loteProdutoFornecedor.get(0).setProduto(produtoMaster);
                loteProdutoFornecedor.get(0).setFornecedor(fornecedorMaster);
                loteProdutoFornecedor.get(0).setProdutoAtivo(false);
        }
        //Se não for nulo e já existir um ativo, ele evita de ir no banco e usa o mesmo produto e fornecedor criando um novo lote.
        if (loteProdutoFornecedor.get(0).getProdutoAtivo()){
            Produto produtoSalvo = loteProdutoFornecedor.get(0).getProduto();
            Fornecedor fornecedorSalvo = loteProdutoFornecedor.get(0).getFornecedor();

            loteProdutoFornecedor = List.of(new ProdutoFornecedor());

            loteProdutoFornecedor.get(0).setProduto(produtoSalvo);
            loteProdutoFornecedor.get(0).setFornecedor(fornecedorSalvo);

        }

        // Dados da carga atual
        if (dto.getEstoqueMinimo().equals(BigDecimal.ZERO)){
            loteProdutoFornecedor.get(0).setEstoqueMinimo(new BigDecimal(0.100));
        }else {
            loteProdutoFornecedor.get(0).setEstoqueMinimo(dto.getEstoqueMinimo() != null ? dto.getEstoqueMinimo() : BigDecimal.valueOf(0.100));
        }
        loteProdutoFornecedor.get(0).setQuantidade(dto.getQuantidade());
        loteProdutoFornecedor.get(0).setDataEntrada(LocalDateTime.now());
        loteProdutoFornecedor.get(0).setDataValidade(dto.getDataValidade());
        loteProdutoFornecedor.get(0).setDataValidadeDias(obterDiasRestantes(dto.getDataValidade()));
        loteProdutoFornecedor.get(0).setPrecoCompra(dto.getPrecoCompra());
        loteProdutoFornecedor.get(0).setPrecoVenda(dto.getPrecoVenda());
        loteProdutoFornecedor.get(0).setUnidadeMedida(dto.getUnidadeMedida());
        loteProdutoFornecedor.get(0).setProdutoAtivo(true);

        // 4. Persistência e Histórico
        ProdutoFornecedor pfSalvo = produtoFornecedorRepository.save(loteProdutoFornecedor.get(0));

        produtoService.atualizarPreco(loteProdutoFornecedor.get(0).getProduto().getId(), dto.getPrecoVenda());
        registrarHistorico(dto, pfSalvo, loteProdutoFornecedor.get(0).getFornecedor());
    }


    @Transactional
    public Venda processarNovaVenda(VendaDTO vendaDTO) {
        Cliente clienteVenda = new Cliente();

        if (vendaDTO.getClienteId() != null){
            clienteVenda = clienteRepository.getReferenceById(vendaDTO.getClienteId());
        }


        List<ItemVenda> itemVendaList = dtoToEntity.itemVendaDtoToEntity(vendaDTO.getItens());
        Venda venda = dtoToEntity.vendaDtoToEntity(vendaDTO, itemVendaList, clienteVenda);

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
                item.setProdutoFornecedor(pf); // Rastreabilidade do lote
                item.setQuantidade(quantidadeAbatidaDesteLote);
                item.setPrecoUnitario(pf.getPrecoVenda());
                item.setSubtotal(pf.getPrecoVenda().multiply(quantidadeAbatidaDesteLote));

                venda.getItens().add(item);

                vendaRepository.save(venda);

                desativarProdutoFornecedor(lotes, null);
            }
        }

        if (venda.getCliente() != null){
         clienteRepository.save(venda.getCliente());
        }
        return venda;
    }

    @Transactional
    public void novaEntradaViaCompras(ItemCompra itemCompra) {
        List<ProdutoFornecedor> produtoFornecedorList = produtoFornecedorRepository.getProdutoFornecedorList(itemCompra.getProdutoId(), null);

        ProdutoFornecedor produtoFornecedorUnico = null;

        for (ProdutoFornecedor pf : produtoFornecedorList) {
            boolean ehOFornecedorDaCompra = pf.getFornecedor() != null && pf.getFornecedor().getId().equals(itemCompra.getProdutoFornecedor().getFornecedor().getId());

            if (ehOFornecedorDaCompra) {
                // Encontramos o vínculo correto!
                produtoFornecedorUnico = pf;
            } else {
                // Se NÃO for o fornecedor da compra, e o estoque estiver zerado (ou <= 0), desativa
                BigDecimal estoquePf = pf.getQuantidade() != null ? pf.getQuantidade() : BigDecimal.ZERO;
                if (estoquePf.compareTo(BigDecimal.ZERO) == 0) {
                    pf.setProdutoAtivo(false);
                    produtoFornecedorRepository.save(pf);
                }
            }
        }

        // 3. Se por acaso o produto nunca teve um vínculo com esse fornecedor, você pode criar um novo aqui
        if (produtoFornecedorUnico == null) {
            produtoFornecedorUnico = new ProdutoFornecedor();
            // Atribui o produto e o fornecedor ao novo objeto
            // vinculoAlvo.setProduto(produtoRepository.findById(produtoId).orElseThrow(...));
            // vinculoAlvo.setFornecedor(fornecedorRepository.findById(fornecedorId).orElseThrow(...));
        }

        // 4. Atualiza os dados do vínculo correto (ativa ele, soma o estoque e atualiza o valor se precisar)
        produtoFornecedorUnico.setProdutoAtivo(true);

        BigDecimal estoqueAtual = produtoFornecedorUnico.getQuantidade() != null ? produtoFornecedorUnico.getQuantidade() : BigDecimal.ZERO;
        produtoFornecedorUnico.setQuantidade(estoqueAtual.add(itemCompra.getQuantidade()));
        produtoFornecedorUnico.setPrecoVenda(BigDecimal.valueOf(itemCompra.getValorVenda()));
        produtoFornecedorUnico.setPrecoCompra(BigDecimal.valueOf(itemCompra.getValorUnitario()));

        produtoService.atualizarPreco(itemCompra.getProdutoId(), BigDecimal.valueOf(itemCompra.getValorVenda()));

        produtoFornecedorRepository.save(produtoFornecedorUnico);

    }


    //REGISTRAR NA TABELA DE HISTORICO.
    private void registrarHistorico(MovimentacaoDTO dto, ProdutoFornecedor pfSalvo, Fornecedor fornecedor) {

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
        mov.setObservacao(dto.getObservacao());

        if (fornecedor != null) {
            mov.setFornecedor(fornecedor);
        }

        movimentacaoRepository.save(mov);
    }


    //REGRA PARA ATIVAR E DESATIVAR O PRODUTO.
    private void desativarProdutoFornecedor(List<ProdutoFornecedor> todosOsLotes, Long produtoId) {
        if (produtoId != null) {
            todosOsLotes = produtoFornecedorRepository.getProdutoFornecedorList(produtoId, PageRequest.of(0, 10));
        }
            List<ProdutoFornecedor> lotesZerados = todosOsLotes.stream()
                    .filter(lp -> lp.getQuantidade().compareTo(BigDecimal.ZERO) <= 0)
                    .toList();

            List<ProdutoFornecedor> lotesComEstoque = todosOsLotes.stream()
                    .filter(lp -> lp.getQuantidade().compareTo(BigDecimal.ZERO) > 0)
                    .toList();

            if (!lotesComEstoque.isEmpty()) {
                lotesZerados.forEach(lote -> lote.setProdutoAtivo(false));
            } else if (!lotesZerados.isEmpty()) {
                ProdutoFornecedor loteAncora = todosOsLotes.stream()
                        .max(Comparator.comparing(ProdutoFornecedor::getDataValidade))
                        .orElse(todosOsLotes.get(0));

                for (ProdutoFornecedor lote : todosOsLotes) {
                    if (lote.equals(loteAncora)) {
                        lote.setProdutoAtivo(true);
                    } else {
                        lote.setProdutoAtivo(false);
                    }
                }
            }
    }

    private String obterDiasRestantes(LocalDate dataValidade) {
        if (dataValidade == null) {
            return "120"; // Evita NullPointerException se a data estiver vazia
        }

        LocalDate hoje = LocalDate.now(); // 2026-10-06

        // Calcula a diferença exata em dias
        String converter = String.valueOf(ChronoUnit.DAYS.between(hoje, dataValidade));

        return converter;
    }
}