package com.farmacia.service;

import com.farmacia.domain.dto.LogResponse;
import com.farmacia.domain.entity.Log;
import com.farmacia.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogConsultaService {

    @Autowired
    private LogRepository logRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Transactional(readOnly = true)
    public List<LogResponse> getUltimos100Logs() {
        List<Log> logs = logRepository.findTop100ByOrderByDataHoraDesc();
        System.out.println("📋 LogConsultaService.getUltimos100Logs() - Total de logs retornados: " + logs.size());
        List<LogResponse> response = logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        System.out.println("📋 LogConsultaService.getUltimos100Logs() - Total de LogResponse: " + response.size());
        return response;
    }

    @Transactional(readOnly = true)
    public List<LogResponse> getUltimos50Logs() {
        List<Log> logs = logRepository.findTop50ByOrderByDataHoraDesc();
        return logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LogResponse> getAllLogs() {
        List<Log> logs = logRepository.findAll();
        return logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportarLogsParaCSV() throws IOException {
        List<LogResponse> logs = getAllLogs();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // Escreve cabeçalho CSV
        writer.println("ID,Tipo Operação,Tipo Entidade,ID Entidade,Descrição,Detalhes,ID Usuário,Nome Usuário,Email Usuário,Data/Hora");

        // Escreve dados
        for (LogResponse log : logs) {
            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    log.getId(),
                    log.getTipoOperacao(),
                    log.getTipoEntidade(),
                    log.getEntidadeId(),
                    escapeCSV(log.getDescricao()),
                    escapeCSV(log.getDetalhes() != null ? log.getDetalhes() : ""),
                    log.getUsuarioId(),
                    escapeCSV(log.getUsuarioNome()),
                    escapeCSV(log.getUsuarioEmail()),
                    log.getDataHora().format(DATE_FORMATTER)
            );
        }

        writer.flush();
        writer.close();

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Escapa aspas duplas e quebras de linha
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
    }

    private LogResponse toResponse(Log log) {
        return new LogResponse(
                log.getId(),
                log.getTipoOperacao(),
                log.getTipoEntidade(),
                log.getEntidadeId(),
                log.getDescricao(),
                log.getDetalhes(),
                log.getUsuarioId(),
                log.getUsuarioNome(),
                log.getUsuarioEmail(),
                log.getDataHora()
        );
    }
}


