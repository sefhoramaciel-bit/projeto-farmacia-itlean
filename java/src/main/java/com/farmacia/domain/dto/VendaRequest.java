package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(
    description = "Dados para criação de venda",
    example = "{\"clienteId\":\"550e8400-e29b-41d4-a716-446655440000\",\"itens\":[{\"medicamentoId\":\"550e8400-e29b-41d4-a716-446655440001\",\"quantidade\":2}]}"
)
public class VendaRequest {

    @NotNull(message = "O campo Cliente é obrigatório, por favor selecione um cliente.")
    @Schema(description = "ID do cliente", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID clienteId;

    @NotEmpty(message = "É necessário adicionar pelo menos um item à venda, por favor adicione um medicamento.")
    @Schema(description = "Lista de itens da venda")
    private List<@Valid ItemVendaRequest> itens;
}





