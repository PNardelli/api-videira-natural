package com.api.uteis;

import com.api.entity.Endereco;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Util {

    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


    public Endereco formatarEndereco(Endereco endereco){
      endereco.setLocalidade(endereco.getLocalidade().trim().toUpperCase());
      endereco.setLogradouro(endereco.getLogradouro().trim().toUpperCase());
      endereco.setEstado(endereco.getEstado().trim().toUpperCase());
      endereco.setBairro(endereco.getBairro().trim().toUpperCase());
      endereco.setComplemento(endereco.getComplemento().trim().toUpperCase());

        return endereco;
    };

}
