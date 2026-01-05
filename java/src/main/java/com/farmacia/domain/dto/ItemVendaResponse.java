package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item da venda")
public class ItemVendaResponse {

    @Schema(description = "ID do item", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID do medicamento", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID medicamentoId;

    @Schema(description = "Nome do medicamento", example = "Dipirona 500mg")
    private String medicamentoNome;

    @Schema(description = "Quantidade", example = "2")
    private Integer quantidade;

    @Schema(description = "Preço unitário", example = "15.50")
    private BigDecimal precoUnitario;

    @Schema(description = "Subtotal", example = "31.00")
    private BigDecimal subtotal;
}









