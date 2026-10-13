package com.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Erro na Operação");
    response.put("message", ex.getMessage()); // Aqui vai sua frase "Estoque insuficiente..."

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }
}
