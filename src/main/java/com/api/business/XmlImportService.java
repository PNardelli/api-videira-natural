package com.api.business;

import com.api.dto.ProdutoRequest;
import com.api.dto.xml.*;
import com.api.eNum.UnidadeMedida;
import com.api.entity.*;
import com.api.repository.CategoriaRepository;
import com.api.repository.FornecedorRepository;
import com.api.repository.ImportacaoXmlRepository;
import com.api.repository.ProdutoRepository;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class XmlImportService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueService estoqueService;

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

    public ImportacaoResultadoDTO prepararConferencia(MultipartFile arquivo) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        // 1. Lê a nota inteira
        NfeCompletaDto nfe = xmlMapper.readValue(arquivo.getInputStream(), NfeCompletaDto.class);

        // --- NOVA LÓGICA: Pega a chave de acesso (Id) ---
        String chaveAcesso = nfe.getNfe().getInfNFe().getChaveNotaFiscal();
        // Verifica se já existe no banco (na sua nova tabela/repository de Notas)
        boolean notaJaExiste = importacaoXmlRepository.existsByIdUnicoNotaFiscal(chaveAcesso);
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
            } else {
                dto.setExisteNoSistema(false);
            }

            listaParaRevisao.add(dto);
        }

        // --- O PULO DO GATO: Retorna o objeto novo "embrulhando" a lista ---
        return new ImportacaoResultadoDTO(notaJaExiste, listaParaRevisao);
    }

    @Transactional
    public void salvarItensRevisados(List<ItemConferenciaDTO> itens) {
        for (ItemConferenciaDTO item : itens) {

            // 1. Se o produto for novo (marcado na tela), a gente cadastra agora
            ProdutoRequest produto = new ProdutoRequest();
            if (item.getProdutoId() == null) {
                produto = new ProdutoRequest();
                if (item.getCategoriaId() != null) {
                    Categoria cat = categoriaRepository.findById(item.getCategoriaId()).orElse(null);
                    produto.setCategoriaId(cat.getId());
                }

                produto.setCodigoProduto(item.getCodigoProdutoInterno());
                produto.setNome(item.getNomeSugerido()); // Nome que você editou no Vue!
                produto.setUnidadeMedida(UnidadeMedida.valueOf(item.getUnidadeMedida().toUpperCase().trim()));
                produto.setCodigoFornecedorXml(Long.valueOf(item.getCodigoFornecedor()));
                produto.setEstoque(item.getQuantidade());
                produto.setPrecoCompra(item.getPrecoCompra());
                produto.setPrecoVenda(item.getPrecoVenda());
                produto.setDataValidade(item.getDataValidade());
                produto.setFornecedorId(item.getFornecedorId());

                Produto produtoSalvo = produtoService.salvar(produto);

                ProdutoFornecedor produtoFornecedorSalvo = produtoFornecedorService.cadastrarProdutoFornecedor(produtoSalvo);

            } else {
                produtoRepository.findById(item.getProdutoId()).get();
            }
        }

        //Salvar Importação!
        ImportacoesXML importacoesXML = new ImportacoesXML();
        importacoesXML.setDataImportacao(LocalDate.now());
        importacoesXML.setIdUnicoNotaFiscal(itens.get(0).getIdUnicoNotaFiscal());
        importacoesXML.setNomeFornecedor("");
        importacaoXmlRepository.save(importacoesXML);

    }
}