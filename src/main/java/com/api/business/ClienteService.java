package com.api.business;

import com.api.dto.ClienteDTO;
import com.api.dto.ClientePdvDTO;
import com.api.eNum.UsuarioRole;
import com.api.entity.Cliente;
import com.api.entity.Endereco;
import com.api.entity.Usuario;
import com.api.repository.ClienteRepository;
import com.api.repository.UsuarioRepository;
import com.api.uteis.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    Util util;

    public Cliente salvarCliente(ClienteDTO clienteDTO) {

        String whatsappLimpo = limparMascara(clienteDTO.getWhatsapp());

        if (clienteDTO.getWhatsapp().isBlank() || clienteDTO.getNome().isBlank()){
            throw new RuntimeException("NOME E WHATSAPP SÃO OBRIGATÓRIOS!");
        }

            if (clienteRepository.existsByWhatsapp(whatsappLimpo)){
                throw new RuntimeException("CLIENTE JÁ CADASTRADO, VERIFIQUE E TENTE NOVAMENTE!");
            }

        Cliente cliente = new Cliente();
        cliente.setNome(clienteDTO.getNome().trim().toUpperCase());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setWhatsapp(whatsappLimpo);
        cliente.setDataNascimento(clienteDTO.getDataNascimento());
        cliente.setDataCriacao(LocalDateTime.now().toString());
        cliente.setAtivo(true);
        if (clienteDTO.getPossuiConta()){
            cliente.setPossuiContaApp(true);
        }else {cliente.setPossuiContaApp(false);}

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
        String whatsappLimpo = limparMascara(clienteAtualizado.getWhatsapp());

        Cliente clienteExistente = clienteRepository.findById(id).orElseThrow();

        clienteExistente.setNome(clienteAtualizado.getNome().trim().toUpperCase());
        clienteExistente.setEmail(clienteAtualizado.getEmail());
        clienteExistente.setWhatsapp(whatsappLimpo);
        clienteExistente.setDataNascimento(clienteAtualizado.getDataNascimento());
        clienteExistente.setPossuiContaApp(clienteAtualizado.getPossuiContaApp());
        clienteExistente.setAtivo(clienteAtualizado.getAtivo());
        clienteExistente.setEndereco(clienteAtualizado.getEndereco() == null ? null : util.formatarEndereco(clienteAtualizado.getEndereco()));
        return clienteRepository.save(clienteExistente);
    }

    public List<Cliente> buscarPorNomeOuWhatsapp(String termo){
        return clienteRepository.buscarPorNomeOuWhatsapp(termo);

    }

    public List<ClientePdvDTO> buscarClientesPdv(String termo) {
        return clienteRepository.buscarPorNomeOuWhatsapp(termo)
                .stream()
                .map(c -> {
                    Endereco endereco = c.getEndereco(); // Exemplo se o relacionamento for direto

                    return new ClientePdvDTO(
                            c.getId(),
                            c.getNome(),
                            c.getWhatsapp(),
                            c.getVideiraSaldo(),
                            endereco != null ? endereco.getCep() : "",
                            endereco != null ? endereco.getLogradouro() : "",
                            endereco != null ? endereco.getNumero() : "",
                            endereco != null ? endereco.getComplemento() : "",
                            endereco != null ? endereco.getBairro() : "",
                            endereco != null ? endereco.getLocalidade() : ""
                    );
                })
                .toList();
    }

    public Cliente cadastrarClienteViaApp(ClienteDTO dto) throws Exception {
        String whatsappLimpo = limparMascara(dto.getWhatsapp());

        try {

        // 1. Busca se o cliente já existe pelo WhatsApp
        Cliente cliente = clienteRepository.buscarPorWhatsapp(whatsappLimpo).orElse(null);

        if (cliente != null) {
            // 2. Se já existe, verifica se ele já tem conta no app
            if (cliente.getPossuiContaApp()) {
                throw new RuntimeException("Este número de WhatsApp já possui uma conta ativa no aplicativo.");
            }

            // Se existe mas veio do PDV sem conta no app, atualizamos os dados faltantes
            cliente.setEmail(dto.getEmail());
            cliente.setDataNascimento(dto.getDataNascimento());
            cliente.setPossuiContaApp(true);
            cliente = clienteRepository.save(cliente);

        } else {
            // 3. Se não existe na base, cria um cliente totalmente novo
            cliente = new Cliente();
            cliente.setNome(dto.getNome());
            cliente.setEmail(dto.getEmail());
            cliente.setWhatsapp(whatsappLimpo);
            cliente.setDataNascimento(dto.getDataNascimento());
            cliente.setDataCriacao(LocalDate.now().toString());
            cliente.setPossuiContaApp(true);
            cliente.setAtivo(true);

            cliente = clienteRepository.save(cliente);
        }

        // 4. Cria o usuário e vincula ao cliente
        Usuario usuario = new Usuario();
        usuario.setLogin(whatsappLimpo);
        usuario.setPassword(dto.getUsuario().getPassword()); // Lembre-se de codificar a senha com BCrypt aqui
        usuario.setDataCriacao(LocalDate.now());
        usuario.setUltimoAcesso(LocalDateTime.now());
        usuario.setRole(UsuarioRole.CLIENTE);
        usuario.setCliente(cliente);
        usuario.setAtivo(true);

        usuarioRepository.save(usuario);

        return cliente;

        }catch (Exception e){
            throw new RuntimeException("Erro ao tentar cadastrar um cliente via APP. ERRO: " + e.getMessage());
        }
    }



    // Função auxiliar para remover tudo que não for dígito
    private String limparMascara(String whatsapp) {
        if (whatsapp == null) {
            return null;
        }
        return whatsapp.replaceAll("\\D", "");
    }

}
