package com.farmacia.domain.dto;

import com.farmacia.domain.enums.StatusVenda;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados da venda")
public class VendaResponse {

    @Schema(description = "ID da venda", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID do cliente", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID clienteId;

    @Schema(description = "Nome do cliente", example = "Maria Santos")
    private String clienteNome;

    @Schema(description = "ID do usuário vendedor", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID usuarioId;

    @Schema(description = "Nome do vendedor", example = "João Silva")
    private String usuarioNome;

    @Schema(description = "Status da venda", example = "CONCLUIDA")
    private StatusVenda status;

    @Schema(description = "Valor total da venda", example = "45.50")
    private BigDecimal valorTotal;

    @Schema(description = "Itens da venda")
    private List<ItemVendaResponse> itens;

    @Schema(description = "Data de criação (formato: dd/MM/yyyy HH:mm:ss)", example = "01/01/2024 10:30:00")
    private LocalDateTime createdAt;
}




