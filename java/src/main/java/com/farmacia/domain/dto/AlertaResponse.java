package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados do alerta")
public class AlertaResponse {

    @Schema(description = "ID do alerta", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID do medicamento", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID medicamentoId;

    @Schema(description = "Nome do medicamento", example = "Dipirona 500mg")
    private String medicamentoNome;

    @Schema(description = "Tipo do alerta", example = "ESTOQUE_BAIXO", allowableValues = {"ESTOQUE_BAIXO", "VALIDADE_PROXIMA"})
    private String tipo;

    @Schema(description = "Mensagem do alerta", example = "Estoque baixo: apenas 5 unidades restantes")
    private String mensagem;

    @Schema(description = "Indica se o alerta foi lido", example = "false")
    private Boolean lido;

    @Schema(description = "Data de criação (formato: dd/MM/yyyy HH:mm:ss)", example = "01/01/2024 10:30:00")
    private LocalDateTime createdAt;
}




