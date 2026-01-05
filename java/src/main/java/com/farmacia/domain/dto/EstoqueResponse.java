package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informações de estoque de um medicamento")
public class EstoqueResponse {

    @Schema(description = "ID do medicamento", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID medicamentoId;

    @Schema(description = "Nome do medicamento", example = "Dipirona 500mg")
    private String medicamentoNome;

    @Schema(description = "Quantidade disponível em estoque", example = "100")
    private Integer quantidadeEstoque;
}






