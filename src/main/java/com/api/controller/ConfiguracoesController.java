package com.api.controller;

import com.api.business.ConfiguracaoFreteService;
import com.api.business.HorarioLojaService;
import com.api.dto.ConfiguracaoFreteDTO;
import com.api.entity.ConfiguracaoFrete;
import com.api.repository.HorarioLojaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/configuracoes")
@CrossOrigin("*")
public class ConfiguracoesController {

    @Autowired
    private ConfiguracaoFreteService service;

    @Autowired
    private HorarioLojaService horarioLojaService;

    @Autowired
    private HorarioLojaRepository lojaRepository;


    //AREA DE FRETE
    @GetMapping("/frete")
    public ResponseEntity<ConfiguracaoFrete> buscar() {
        ConfiguracaoFrete config = service.obterConfiguracao();
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    @PutMapping("/cadastrar-frete")
    public ResponseEntity<ConfiguracaoFrete> salvar(@RequestBody ConfiguracaoFreteDTO dto) {
        ConfiguracaoFrete salva = service.salvarOuAtualizar(dto);
        return ResponseEntity.ok(salva);
    }

    //AREA DE HORARIOS
    @GetMapping("/loja")
    public ResponseEntity<java.util.Map<String, Object>> buscarHorarioLoja() {
        com.api.entity.HorarioLoja config = lojaRepository.findById(1L).orElseGet(() -> {
            com.api.entity.HorarioLoja padrao = new com.api.entity.HorarioLoja();
            padrao.setId(1L);
            padrao.setFechadoManual(false);
            padrao.setHorariosJson("["
                    + "{\"dia\":\"SEGUNDA\",\"label\":\"Segunda-feira\",\"habilitado\":true,\"abre\":\"08:00\",\"fecha\":\"18:00\"},"
                    + "{\"dia\":\"TERCA\",\"label\":\"Terça-feira\",\"habilitado\":true,\"abre\":\"08:00\",\"fecha\":\"18:00\"},"
                    + "{\"dia\":\"QUARTA\",\"label\":\"Quarta-feira\",\"habilitado\":true,\"abre\":\"08:00\",\"fecha\":\"18:00\"},"
                    + "{\"dia\":\"QUINTA\",\"label\":\"Quinta-feira\",\"habilitado\":true,\"abre\":\"08:00\",\"fecha\":\"18:00\"},"
                    + "{\"dia\":\"SEXTA\",\"label\":\"Sexta-feira\",\"habilitado\":true,\"abre\":\"08:00\",\"fecha\":\"18:00\"},"
                    + "{\"dia\":\"SABADO\",\"label\":\"Sábado\",\"habilitado\":true,\"abre\":\"08:00\",\"fecha\":\"13:00\"},"
                    + "{\"dia\":\"DOMINGO\",\"label\":\"Domingo\",\"habilitado\":false,\"abre\":\"08:00\",\"fecha\":\"12:00\"}"
                    + "]");
            return padrao;
        });

        boolean abertaAgora = calcularLojaAberta(config);

        java.util.Map<String, Object> resposta = new java.util.HashMap<>();
        resposta.put("fechadoManual", Boolean.TRUE.equals(config.getFechadoManual()));
        resposta.put("horariosJson", config.getHorariosJson());
        resposta.put("aberta", abertaAgora);

        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/loja")
    public ResponseEntity<java.util.Map<String, Object>> salvarHorarioLoja(@RequestBody java.util.Map<String, Object> body) {
        com.api.entity.HorarioLoja config = lojaRepository.findById(1L).orElse(new com.api.entity.HorarioLoja());
        config.setId(1L);
        config.setFechadoManual(Boolean.TRUE.equals(body.get("fechadoManual")));
        config.setHorariosJson(body.get("horariosJson") != null ? String.valueOf(body.get("horariosJson")) : "[]");
        lojaRepository.save(config);

        return buscarHorarioLoja();

    }

    //METODOS PRIVADOS
    private boolean calcularLojaAberta(com.api.entity.HorarioLoja config) {
        if (Boolean.TRUE.equals(config.getFechadoManual())) {
            return false;
        }
        try {
            java.time.ZonedDateTime agora = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Sao_Paulo"));
            java.time.DayOfWeek dow = agora.getDayOfWeek();
            String diaAtual = switch (dow) {
                case MONDAY -> "SEGUNDA";
                case TUESDAY -> "TERCA";
                case WEDNESDAY -> "QUARTA";
                case THURSDAY -> "QUINTA";
                case FRIDAY -> "SEXTA";
                case SATURDAY -> "SABADO";
                case SUNDAY -> "DOMINGO";
            };

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> dias = mapper.readValue(
                    config.getHorariosJson(),
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
            );

            java.time.LocalTime horaAtual = agora.toLocalTime();
            for (java.util.Map<String, Object> item : dias) {
                if (diaAtual.equals(String.valueOf(item.get("dia")))) {
                    boolean habilitado = Boolean.TRUE.equals(item.get("habilitado"));
                    if (!habilitado) return false;
                    java.time.LocalTime abre = java.time.LocalTime.parse(String.valueOf(item.get("abre")));
                    java.time.LocalTime fecha = java.time.LocalTime.parse(String.valueOf(item.get("fecha")));
                    return !horaAtual.isBefore(abre) && horaAtual.isBefore(fecha);
                }
            }
        } catch (Exception e) {
            return true;
        }
        return false;
    }

}
