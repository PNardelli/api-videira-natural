package com.api.business;

import com.api.dto.*;
import com.api.entity.Produto;
import com.api.repository.CategoriaRepository;
import com.api.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class XmlImportService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public ImportacaoNfeResponse processarXml(MultipartFile arquivo) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        NfeCompletaDto nfe = xmlMapper.readValue(arquivo.getInputStream(), NfeCompletaDto.class);

        ImportacaoNfeResponse response = new ImportacaoNfeResponse();

        if (nfe != null && nfe.getNfe() != null && nfe.getNfe().getInfNFe() != null) {

            EmitenteDto fornecedor = nfe.getNfe().getInfNFe().getEmitente();
            if (fornecedor != null) {
                System.out.println("Importando nota de: " + fornecedor.getNome());
            }

            if (nfe.getNfe().getInfNFe().getItens() != null) {
                for (xmlDTO itemNfe : nfe.getNfe().getInfNFe().getItens()) {

                    String codigoBusca = itemNfe.getCodigoBarra();

                    if ("SEM GTIN".equalsIgnoreCase(codigoBusca) || codigoBusca == null) {
                        codigoBusca = itemNfe.getProd().getCodigoFornecedor();
                    }

                    Optional<Produto> produtoExistente = produtoRepository.findByCodigoBarras(codigoBusca);

                    if (produtoExistente.isPresent()) {
                        response.addProdutoExistente(produtoExistente.get(), itemNfe.getProd().getQuantidade());
                    } else {
                        response.addProdutoNovo(itemNfe);
                    }
                }
            }
        } else {
            throw new IOException("Arquivo XML inválido ou fora do padrão NF-e.");
        }

        return response;
    }

    @Transactional
    public void confirmarImportacao(ConfirmarImportacaoRequest request) {

    }
}