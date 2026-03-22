package com.api.business;

import com.api.entity.Fornecedor;
import com.api.repository.FornecedorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FornecedorService {

    private final FornecedorRepository repository;

    public FornecedorService(FornecedorRepository repository) {
        this.repository = repository;
    }

    public ResponseEntity<Fornecedor> salvar(Fornecedor payload) {
            if (payload.getNome().isBlank() || payload.getCpfCnpj().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome ou CPF/CNPJ obrigatórios.");
        }

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(payload.getNome());
        fornecedor.setWhatsapp(payload.getWhatsapp());
        fornecedor.setCpfCnpj(payload.getCpfCnpj());

        repository.save(fornecedor);

        return ResponseEntity.ok(fornecedor);
    }

    public List<Fornecedor> listar() {
        return repository.findAll();
    }

}
