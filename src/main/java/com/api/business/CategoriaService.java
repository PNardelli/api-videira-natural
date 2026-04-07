package com.api.business;

import com.api.dto.CategoriaDTO;
import com.api.entity.Categoria;
import com.api.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public Categoria salvar(CategoriaDTO categoriaDTO) {

        Categoria categoria = new Categoria();
        categoria.setNome(categoriaDTO.getNome().trim().toUpperCase());

        return repository.save(categoria);
    }

    public List<Categoria> listar() {
        return repository.findAll();
    }

}
