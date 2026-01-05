package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta com mensagem de operação")
public class MensagemResponse {

    @Schema(description = "Mensagem da operação", example = "Deletado com sucesso")
    private String mensagem;

    @Schema(description = "ID da venda (quando aplicável)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
}






