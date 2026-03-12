package com.api.business;

import com.api.dto.ClienteDTO;
import com.api.entity.Cliente;
import com.api.entity.Endereco;
import com.api.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    ClienteRepository clienteRepository;

    public Cliente salvarCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public void deletar(Long id) {
        clienteRepository.deleteById(id);
    }

    public void alterarStatus(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        cliente.setAtivo(!cliente.getAtivo());

        clienteRepository.save(cliente);
    }

    public Cliente atualizarCliente(Long id, Cliente clienteAtualizado) {
        Cliente clienteExistente = clienteRepository.findById(id).orElseThrow();

        clienteExistente.setNome(clienteAtualizado.getNome());
        clienteExistente.setEmail(clienteAtualizado.getEmail());
        clienteExistente.setWhatsapp(clienteAtualizado.getWhatsapp());
        clienteExistente.setPossuiContaApp(clienteAtualizado.getPossuiContaApp());
        clienteExistente.setAtivo(clienteAtualizado.getAtivo());

        if (clienteAtualizado.getEndereco().getCep() != null) {
            clienteExistente.setEndereco(clienteAtualizado.getEndereco());
        }

        return clienteRepository.save(clienteExistente);
    }
}
