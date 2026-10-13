package com.api.business;

import com.fazecast.jSerialComm.SerialPort;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class BalancaService {

    private volatile double pesoAtual = 0.0;
    private volatile boolean estavel = false;

    private final AtomicBoolean lendo = new AtomicBoolean(false);

    private SerialPort porta;
    private StringBuilder buffer = new StringBuilder();

    // CONFIG
    private static final String PORTA = "COM3"; // alterar se necessário
    private static final int BAUD_RATE = 9600;

    public void iniciarLeitura() {
        if (lendo.get()) return;

        porta = SerialPort.getCommPort(PORTA);
        porta.setBaudRate(BAUD_RATE);
        porta.setNumDataBits(8);
        porta.setNumStopBits(1);
        porta.setParity(SerialPort.NO_PARITY);

        if (!porta.openPort()) {
            throw new RuntimeException("Erro ao abrir porta serial: " + PORTA);
        }

        lendo.set(true);

        new Thread(() -> {
            byte[] readBuffer = new byte[1024];

            while (lendo.get()) {
                try {
                    int numRead = porta.readBytes(readBuffer, readBuffer.length);

                    if (numRead > 0) {
                        String chunk = new String(readBuffer, 0, numRead, StandardCharsets.UTF_8);
                        acumularEProcessar(chunk);
                    }

                    Thread.sleep(50);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            porta.closePort();
        }).start();
    }

    public void pararLeitura() {
        lendo.set(false);
    }

    // Junta pedaços até formar uma linha completa
    private void acumularEProcessar(String chunk) {
        buffer.append(chunk);

        int index;
        while ((index = buffer.indexOf("\n")) != -1) {
            String linha = buffer.substring(0, index).trim();
            buffer.delete(0, index + 1);

            processarLeitura(linha);
        }
    }

    private void processarLeitura(String raw) {
        try {
            if (raw.isEmpty()) return;

            // Ex: "ST,GS,  1.234 kg"
            estavel = raw.contains("ST");

            double peso = parsePeso(raw);

            if (peso >= 0) {
                atualizarPeso(peso);
            }

        } catch (Exception ignored) {
        }
    }

    private double parsePeso(String raw) {
        try {
            String numero = raw
                    .replaceAll("[^0-9,\\.]", "")
                    .replace(",", ".");

            if (numero.isEmpty()) return -1;

            return Double.parseDouble(numero);

        } catch (Exception e) {
            return -1;
        }
    }

    private void atualizarPeso(double novoPeso) {
        this.pesoAtual = novoPeso;
    }

    public double getPeso() {
        return pesoAtual;
    }

    public boolean isEstavel() {
        return estavel;
    }
}