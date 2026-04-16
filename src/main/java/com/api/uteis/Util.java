package com.api.uteis;

import com.api.entity.Endereco;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

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

    public String GeradorCodigo (){

        LocalDateTime data = LocalDateTime.now();

        var hora = data.getHour();
        var min = data.getMinute();
        var seg = data.getSecond();

        String prefixo = String.valueOf(hora+min+seg);
        return prefixo;
    }

}
