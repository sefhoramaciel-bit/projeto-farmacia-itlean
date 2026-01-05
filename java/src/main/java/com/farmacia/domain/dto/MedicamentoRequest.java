package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(
    description = "Dados para criação/atualização de medicamento",
    example = "{\"nome\":\"Dipirona 500mg\",\"descricao\":\"Medicamento analgésico e antitérmico\",\"preco\":15.50,\"quantidadeEstoque\":100,\"validade\":\"2025-12-31\",\"ativo\":true,\"categoriaId\":\"550e8400-e29b-41d4-a716-446655440000\"}"
)
public class MedicamentoRequest {

    @NotBlank(message = "O campo Nome é obrigatório, por favor preencha.")
    @Schema(description = "Nome do medicamento (obrigatório, único)", example = "Dipirona 500mg")
    private String nome;

    @Schema(description = "Descrição do medicamento", example = "Medicamento analgésico e antitérmico")
    private String descricao;

    @NotNull(message = "O campo Preço é obrigatório, por favor preencha.")
    @Positive(message = "O campo Preço deve ser maior que zero, por favor alterar.")
    @Schema(description = "Preço unitário (obrigatório, deve ser maior que zero)", example = "15.50")
    private BigDecimal preco;

    @NotNull(message = "O campo Quantidade em Estoque é obrigatório, por favor preencha.")
    @Min(value = 0, message = "O campo Quantidade em Estoque não pode ser negativa, por favor alterar.")
    @Schema(description = "Quantidade disponível em estoque (obrigatório, mínimo: 0)", example = "100")
    private Integer quantidadeEstoque;

    @NotNull(message = "O campo Data de Validade é obrigatório, por favor preencha.")
    @Future(message = "O campo Data de Validade deve ser uma data futura, por favor alterar.")
    @Schema(description = "Data de validade (obrigatório, formato: YYYY-MM-DD, deve ser futura)", example = "2025-12-31")
    private LocalDate validade;

    @Schema(description = "Status ativo", example = "true")
    private Boolean ativo = true;

    @NotNull(message = "O campo Categoria é obrigatório, por favor preencha.")
    @Schema(description = "ID da categoria (obrigatório)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID categoriaId;
}




