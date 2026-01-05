package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Item da venda")
public class ItemVendaRequest {

    @NotNull(message = "O campo Medicamento é obrigatório, por favor selecione um medicamento.")
    @Schema(description = "ID do medicamento", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID medicamentoId;

    @NotNull(message = "O campo Quantidade é obrigatório, por favor preencha.")
    @Min(value = 1, message = "O campo Quantidade deve ser maior que zero, por favor alterar.")
    @Schema(description = "Quantidade do item", example = "2")
    private Integer quantidade;
}





