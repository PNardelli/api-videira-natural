package com.api.controller;

import com.api.entity.Endereco;
import com.api.uteis.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/endereco")
@CrossOrigin(origins = "*") // libera para o Vue local
public class EnderecoController {

    @Autowired
    Util util;

    @GetMapping("/{cep}")
    public ResponseEntity<Endereco> buscarCep(@PathVariable String cep){
        String url = "https://viacep.com.br/ws/" + cep + "/json/";

        Endereco endereco = util.restTemplate().getForObject(url, Endereco.class);

        return ResponseEntity.ok(endereco);


    }


}
