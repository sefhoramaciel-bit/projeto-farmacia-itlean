package com.farmacia.controller;

import com.farmacia.domain.dto.AlertaResponse;
import com.farmacia.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas", description = "Endpoints para gerenciamento de alertas")
@SecurityRequirement(name = "Bearer Authentication")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @GetMapping
    @Operation(summary = "Listar alertas", description = "Retorna todos os alertas (ADMIN e VENDEDOR)")
    public ResponseEntity<List<AlertaResponse>> findAll() {
        List<AlertaResponse> response = alertaService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nao-lidos")
    @Operation(summary = "Listar alertas não lidos", description = "Retorna apenas alertas não lidos (ADMIN e VENDEDOR)")
    public ResponseEntity<List<AlertaResponse>> findNaoLidos() {
        List<AlertaResponse> response = alertaService.findNaoLidos();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estoque-baixo")
    @Operation(summary = "Listar alertas de estoque baixo", description = "Retorna alertas de estoque baixo não lidos (ADMIN e VENDEDOR)")
    public ResponseEntity<List<AlertaResponse>> findEstoqueBaixo() {
        List<AlertaResponse> response = alertaService.findEstoqueBaixo();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validade-proxima")
    @Operation(summary = "Listar alertas de validade próxima", description = "Retorna alertas de validade próxima não lidos (ADMIN e VENDEDOR)")
    public ResponseEntity<List<AlertaResponse>> findValidadeProxima() {
        List<AlertaResponse> response = alertaService.findValidadeProxima();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validade-vencida")
    @Operation(summary = "Listar alertas de validade vencida", description = "Retorna alertas de medicamentos vencidos não lidos (ADMIN e VENDEDOR)")
    public ResponseEntity<List<AlertaResponse>> findValidadeVencida() {
        List<AlertaResponse> response = alertaService.findValidadeVencida();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/ler")
    @Operation(summary = "Marcar alerta como lido", description = "Marca um alerta como lido (ADMIN e VENDEDOR)")
    public ResponseEntity<AlertaResponse> marcarComoLido(@PathVariable UUID id) {
        AlertaResponse response = alertaService.marcarComoLido(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/gerar")
    @Operation(summary = "Gerar alertas manualmente", description = "Força a geração de alertas de estoque baixo e validade (ADMIN e VENDEDOR)")
    public ResponseEntity<String> gerarAlertas() {
        alertaService.gerarAlertasManual();
        return ResponseEntity.ok("Alertas gerados com sucesso");
    }
}



