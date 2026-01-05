package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(
    description = "Dados para movimentação de estoque",
    example = "{\"medicamentoId\":\"550e8400-e29b-41d4-a716-446655440000\",\"quantidade\":10,\"motivo\":\"Entrada de estoque\"}"
)
public class EstoqueRequest {

    @NotNull(message = "ID do medicamento é obrigatório")
    @Schema(description = "ID do medicamento (obrigatório)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID medicamentoId;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser positiva")
    @Schema(description = "Quantidade a movimentar (obrigatório, > 0)", example = "10")
    private Integer quantidade;

    @Schema(description = "Motivo da movimentação (opcional)", example = "Entrada de estoque")
    private String motivo;
}






