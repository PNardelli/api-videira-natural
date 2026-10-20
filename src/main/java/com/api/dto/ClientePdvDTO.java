package com.api.dto;

public record ClientePdvDTO (
     Long id,
     String nome,
     String whatsapp,
     Integer saldo,
     String cep,
     String logradouro,
     String numero,
     String complemento,
     String bairro,
     String localidade
){}
