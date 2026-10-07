package com.api.business;

import com.api.entity.Fornecedor;
import com.api.repository.FornecedorRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class FornecedorService {

    private final FornecedorRepository repository;

    public FornecedorService(FornecedorRepository repository) {
        this.repository = repository;
    }


    @CacheEvict(value = "fornecedores", allEntries = true)
    public ResponseEntity<Fornecedor> salvar(Fornecedor payload) {
            if (payload.getNome().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome obrigatório.");
        }

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(payload.getNome().trim().toUpperCase());
        fornecedor.setWhatsapp(payload.getWhatsapp());
        fornecedor.setCpfCnpj(payload.getCpfCnpj());
        fornecedor.setDataCadastro(LocalDate.now());

        repository.save(fornecedor);

        return ResponseEntity.ok(fornecedor);
    }

    @Cacheable(value = "fornecedores")
    public List<Fornecedor> listar() {
        return repository.findAll();
    }


    public Fornecedor atualizarFornecedor(Long id, Fornecedor fornecedorAtualizado) {
        Fornecedor fornecedorParaAtualizar = repository.findById(id).orElseThrow();

        fornecedorParaAtualizar.setNome(fornecedorAtualizado.getNome().trim().toUpperCase());
        fornecedorParaAtualizar.setCpfCnpj(fornecedorAtualizado.getCpfCnpj().trim());
        fornecedorParaAtualizar.setWhatsapp(fornecedorAtualizado.getWhatsapp().trim());

        return fornecedorParaAtualizar;
    }

}
