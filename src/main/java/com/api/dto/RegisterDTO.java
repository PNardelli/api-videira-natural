package com.api.dto;

import com.api.eNum.UsuarioRole;

public record RegisterDTO(String login, String senha, UsuarioRole role) {

}
