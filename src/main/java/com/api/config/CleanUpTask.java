package com.api.config;

import com.api.repository.OrcamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@EnableScheduling
public class CleanUpTask {

    @Autowired
    private OrcamentoRepository repository;

    // Roda a cada 1 hora para manter o banco leve
    @Scheduled(cron = "0 0 * * * *")
    public void execute() {
        repository.deleteExpiredOrcamentos(LocalDateTime.now().minusDays(1));
    }
}
