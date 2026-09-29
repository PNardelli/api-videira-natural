package com.api.business;

import com.api.dto.ParceirosDTO;
import com.api.entity.Parceiros;
import com.api.repository.ParceirosRepository;
import com.api.uteis.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ParceiroService {

    @Autowired
    ParceirosRepository parceirosRepository;

    @Autowired
    Util util;

    public Parceiros salvarParceiro(ParceirosDTO parceirosDTO) {

        if (parceirosDTO.getNomeParceiro().isBlank()){
            throw new RuntimeException("NOME DO PARCEIRO É OBRIGATÓRIO!");
        }
        
        Parceiros parceiro = new Parceiros();
        parceiro.setNomeParceiro(parceirosDTO.getNomeParceiro());
        parceiro.setPercentualDesconto(parceirosDTO.getPercentualDesconto());
        parceiro.setInstagram(parceirosDTO.getInstagram());
        parceiro.setDataCadastro(LocalDate.now());
        parceiro.setAtivo(true);

        return parceirosRepository.save(parceiro);
    }

    public List<Parceiros> listarTodos() {
        return parceirosRepository.findAll();
    }

    public Optional<Parceiros> buscarPorId(Long id) {
        return parceirosRepository.findById(id);
    }

    public void alterarStatus(Long id) {
        Parceiros parceiros = parceirosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parceiro não encontrado"));

        parceiros.setAtivo(!parceiros.isAtivo());

        parceirosRepository.save(parceiros);
    }

    public Parceiros atualizarParceiros(Long id, Parceiros parceirosAtualizado) {
        Parceiros parceirosExistente = parceirosRepository.findById(id).orElseThrow();


        parceirosExistente.setNomeParceiro(parceirosAtualizado.getNomeParceiro().trim().toUpperCase());
        parceirosExistente.setInstagram(parceirosAtualizado.getInstagram());
        parceirosExistente.setPercentualDesconto(parceirosAtualizado.getPercentualDesconto());

        return parceirosRepository.save(parceirosExistente);
    }

}
