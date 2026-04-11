package com.api.business;

import com.api.dto.ProdutoRequest;
import com.api.dto.xml.*;
import com.api.eNum.TipoMovimentacao;
import com.api.eNum.UnidadeMedida;
import com.api.entity.*;
import com.api.exceptions.NotaJaImportadaException;
import com.api.repository.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class XmlImportService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private ProdutoFornecedorService produtoFornecedorService;

    @Autowired
    private ImportacaoXmlRepository importacaoXmlRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Transactional
    public ImportacaoResultadoDTO prepararConferencia(MultipartFile arquivo) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        // 1. Lê a nota inteira
        NfeCompletaDto nfe = xmlMapper.readValue(arquivo.getInputStream(), NfeCompletaDto.class);

        // --- NOVA LÓGICA: Pega a chave de acesso (Id) ---
        String chaveAcesso = nfe.getNfe().getInfNFe().getChaveNotaFiscal();
        // Verifica se já existe no banco (na sua nova tabela/repository de Notas)
        boolean notaJaExiste = importacaoXmlRepository.existsByIdUnicoNotaFiscal(chaveAcesso);

        if (notaJaExiste) {
            importacaoXmlRepository.deleteByIdUnicoNotaFiscal(chaveAcesso);
        }
        // -----------------------------------------------

        EmitenteDto fornecedorViaXml = nfe.getNfe().getInfNFe().getEmitente();

        var fornecedor = fornecedorRepository.findByCpfCnpj(fornecedorViaXml.getCnpj()).orElseGet(() -> {
            Fornecedor novoFornecedor = new Fornecedor();
            novoFornecedor.setNome(fornecedorViaXml.getNome());
            novoFornecedor.setWhatsapp(fornecedorViaXml.getEnderEmit().getTelefone());
            novoFornecedor.setCpfCnpj(fornecedorViaXml.getCnpj());
            novoFornecedor.setDataCadastro(LocalDate.now());
            return fornecedorRepository.save(novoFornecedor);
        });

        List<ItemConferenciaDTO> listaParaRevisao = new ArrayList<>();

        for (ItemNotaDTO itemXml : nfe.getNfe().getInfNFe().getItens()) {
            ProdutoXmlDTO prodXml = itemXml.getProduto();

            ItemConferenciaDTO dto = new ItemConferenciaDTO();
            // Seta a chave da nota em cada item (útil para o DTO que você já tem)
            dto.setIdUnicoNotaFiscal(chaveAcesso);
            dto.setCodigoFornecedor(prodXml.getCodigoProduto());
            dto.setNomeOriginalXml(prodXml.getNome());
            dto.setFornecedorId(fornecedor.getId());

            String nomeLimpo = prodXml.getNome()
                    .replace("A GRANEL", "")
                    .replace("ATACADO", "")
                    .trim().toUpperCase();
            dto.setNomeSugerido(nomeLimpo);

            dto.setQuantidade(prodXml.getQuantidade());
            dto.setPrecoCompra(prodXml.getValorCusto());
            dto.setUnidadeMedida(prodXml.getUnidadeMedida());

            Optional<Produto> produtoExistente = produtoRepository
                    .findByCodigoFornecedorXml(dto.getCodigoFornecedor());

            if (produtoExistente.isPresent()) {
                dto.setExisteNoSistema(true);
                dto.setProdutoId(produtoExistente.get().getId());
                dto.setNomeSugerido(produtoExistente.get().getNome());
                dto.setPrecoCompra(produtoExistente.get().getPrecoCompra());
                dto.setCategoriaId(produtoExistente.get().getCategoria().getId());
                dto.setPrecoVenda(produtoExistente.get().getPrecoVenda());
                dto.setCodigoProdutoInterno(produtoExistente.get().getCodigoProduto());
                dto.setDataValidade(produtoExistente.get().getDataValidade());
                dto.setUnidadeMedida(produtoExistente.get().getUnidadeMedida().name());
            } else {
                dto.setExisteNoSistema(false);
            }

            listaParaRevisao.add(dto);
        }

        // --- O PULO DO GATO: Retorna o objeto novo "embrulhando" a lista ---
        return new ImportacaoResultadoDTO(notaJaExiste, listaParaRevisao);
    }

    @Transactional
    public void salvarItensRevisados(List<ItemConferenciaDTO> itens, Boolean reimportar) {
        String chaveNota = itens.get(0).getIdUnicoNotaFiscal();

        boolean jaExiste = importacaoXmlRepository.existsByIdUnicoNotaFiscal(chaveNota);

        if (jaExiste && Boolean.TRUE.equals(reimportar)) {
            importacaoXmlRepository.deleteByIdUnicoNotaFiscal(chaveNota);
        }

        for (ItemConferenciaDTO item : itens) {

            MovimentacaoEstoque movimentacaoEstoque = new MovimentacaoEstoque();

            // 1. Se o produto for novo (marcado na tela), a gente cadastra agora
            if (item.getProdutoId() == null) {
                ProdutoRequest produtoRequest = new ProdutoRequest();
                if (item.getCategoriaId() != null) {
                    Categoria cat = categoriaRepository.findById(item.getCategoriaId()).orElse(null);
                    produtoRequest.setCategoriaId(cat.getId());
                }

                produtoRequest.setCodigoProduto(item.getCodigoProdutoInterno());
                produtoRequest.setNome(item.getNomeSugerido()); // Nome que você editou no Vue!
                produtoRequest.setUnidadeMedida(UnidadeMedida.valueOf(item.getUnidadeMedida().toUpperCase().trim()));
                produtoRequest.setCodigoFornecedorXml(Long.valueOf(item.getCodigoFornecedor()));
                produtoRequest.setEstoque(item.getQuantidade());
                produtoRequest.setPrecoCompra(item.getPrecoCompra());
                produtoRequest.setPrecoVenda(item.getPrecoVenda());
                produtoRequest.setDataValidade(item.getDataValidade());

                Produto produtoSalvo = produtoService.salvar(produtoRequest);

                ProdutoFornecedor produtoFornecedorSalvo = produtoFornecedorService.cadastrarProdutoFornecedor(produtoSalvo, item.getFornecedorId());

                movimentacaoEstoque.setUnidadeMedida(produtoFornecedorSalvo.getUnidade());
                movimentacaoEstoque.setDataValidade(produtoFornecedorSalvo.getDataValidade());
                movimentacaoEstoque.setPrecoVenda(produtoFornecedorSalvo.getPrecoVenda());
                movimentacaoEstoque.setPrecoCompra(produtoFornecedorSalvo.getPrecoCompra());
                movimentacaoEstoque.setFornecedor(produtoFornecedorSalvo.getFornecedor());
                movimentacaoEstoque.setData(LocalDateTime.now());
                movimentacaoEstoque.setProduto(produtoSalvo);
                movimentacaoEstoque.setNotaFiscal(chaveNota);
                movimentacaoEstoque.setObservacao("ENTRADA DE PRODUTOS VIA NOTA FISCAL");
                movimentacaoEstoque.setOrigemMovimentacao("XML");
                movimentacaoEstoque.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
                movimentacaoEstoque.setQuantidade(produtoFornecedorSalvo.getQuantidade());

              movimentacaoEstoqueRepository.save(movimentacaoEstoque);


            } else {
                produtoRepository.findById(item.getProdutoId());
            }
        }

        //CHAMA SALVAR IMPORTACÃO
        salvarImportacao(chaveNota, itens.get(0).getFornecedorId());
    }

    private void salvarImportacao(String chaveNota, Long idFornecedor) {
        //Salvar Importação!
        ImportacoesXML importacoesXML = new ImportacoesXML();
        importacoesXML.setDataImportacao(LocalDate.now());
        importacoesXML.setIdUnicoNotaFiscal(chaveNota);

        var fornecedor = fornecedorRepository.findById(idFornecedor).orElseThrow();
        importacoesXML.setNomeFornecedor(fornecedor.getNome());

        try {
            importacaoXmlRepository.save(importacoesXML);
        } catch (DataIntegrityViolationException e) {
            throw new NotaJaImportadaException(importacoesXML.getIdUnicoNotaFiscal());
        }
    }
}