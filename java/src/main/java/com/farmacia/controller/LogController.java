package com.farmacia.controller;

import com.farmacia.domain.dto.LogResponse;
import com.farmacia.service.LogConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Logs", description = "Endpoints para consulta de logs de auditoria")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class LogController {

    @Autowired
    private LogConsultaService logConsultaService;

    @GetMapping
    @Operation(summary = "Listar últimos 100 logs", 
               description = "Retorna os últimos 100 registros de log ordenados por data/hora (mais recentes primeiro). (apenas ADMIN)")
    public ResponseEntity<List<LogResponse>> getUltimos100Logs() {
        List<LogResponse> logs = logConsultaService.getUltimos100Logs();
        System.out.println("📋 LogController.getUltimos100Logs() - Retornando " + logs.size() + " logs");
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/export")
    @Operation(summary = "Exportar todos os logs para CSV", 
               description = "Gera e faz download de um arquivo CSV com TODOS os registros de log do banco de dados. (apenas ADMIN)")
    public ResponseEntity<InputStreamResource> exportarLogs() {
        try {
            ByteArrayInputStream csvStream = logConsultaService.exportarLogsParaCSV();
            
            String fileName = "logs_auditoria_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + fileName);
            headers.add("Content-Type", "text/csv; charset=UTF-8");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(new InputStreamResource(csvStream));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


