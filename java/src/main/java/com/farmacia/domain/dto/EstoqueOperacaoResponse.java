package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de operação de estoque (entrada ou saída)")
public class EstoqueOperacaoResponse {

    @Schema(description = "Mensagem da operação", example = "Estoque aumentado com sucesso")
    private String mensagem;

    @Schema(description = "ID do medicamento", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID medicamentoId;

    @Schema(description = "Nome do medicamento", example = "Dipirona 500mg")
    private String medicamentoNome;

    @Schema(description = "Quantidade movimentada", example = "10")
    private Integer quantidadeMovimentada;

    @Schema(description = "Quantidade total em estoque após a operação", example = "110")
    private Integer quantidadeEstoqueAtual;

    @Schema(description = "Tipo da operação", example = "ENTRADA", allowableValues = {"ENTRADA", "SAIDA"})
    private String tipoOperacao;
}






