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
@Schema(description = "Dados da categoria")
public class CategoriaResponse {

    @Schema(description = "ID da categoria", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Nome da categoria", example = "Analgésicos")
    private String nome;

    @Schema(description = "Descrição da categoria", example = "Medicamentos para dor")
    private String descricao;

    @Schema(description = "Data de criação (formato: dd/MM/yyyy HH:mm:ss)", example = "01/01/2024 10:30:00")
    private LocalDateTime createdAt;
}




