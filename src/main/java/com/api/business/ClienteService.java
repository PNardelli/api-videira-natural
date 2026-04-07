package com.api.business;

import com.api.dto.ClienteDTO;
import com.api.entity.Cliente;
import com.api.entity.Endereco;
import com.api.repository.ClienteRepository;
import com.api.uteis.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    Util util;

    public Cliente salvarCliente(ClienteDTO clienteDTO) {

        if (clienteDTO.getWhatsapp().isBlank() || clienteDTO.getNome().isBlank()){
            throw new RuntimeException("NOME E WHATSAPP SÃO OBRIGATÓRIOS!");
        }

        if (clienteRepository.existsByWhatsappOrEmail(clienteDTO.getWhatsapp(), clienteDTO.getEmail())){
            throw new RuntimeException("CLIENTE JÁ CADASTRADO, VERIFIQUE E TENTE NOVAMENTE!");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(clienteDTO.getNome().trim().toUpperCase());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setWhatsapp(clienteDTO.getWhatsapp());
        cliente.setDataNascimento(clienteDTO.getDataNascimento());
        cliente.setDataCriacao(LocalDateTime.now().toString());
        cliente.setAtivo(true);
        cliente.setPossuiContaApp(false);
        cliente.setEndereco(clienteDTO.getEndereco() == null ? null : util.formatarEndereco(clienteDTO.getEndereco()));


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


        clienteExistente.setNome(clienteAtualizado.getNome().trim().toUpperCase());
        clienteExistente.setEmail(clienteAtualizado.getEmail());
        clienteExistente.setWhatsapp(clienteAtualizado.getWhatsapp());
        clienteExistente.setDataNascimento(clienteAtualizado.getDataNascimento());
        clienteExistente.setPossuiContaApp(clienteAtualizado.getPossuiContaApp());
        clienteExistente.setAtivo(clienteAtualizado.getAtivo());
        clienteExistente.setEndereco(clienteAtualizado.getEndereco() == null ? null : util.formatarEndereco(clienteAtualizado.getEndereco()));
        return clienteRepository.save(clienteExistente);
    }
}
