package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(
    description = "Dados para criação/atualização de categoria",
    example = "{\"nome\":\"Analgésicos\",\"descricao\":\"Medicamentos para dor\"}"
)
public class CategoriaRequest {

    @NotBlank(message = "O campo Nome é obrigatório, por favor preencha.")
    @Schema(description = "Nome da categoria", example = "Analgésicos")
    private String nome;

    @Schema(description = "Descrição da categoria", example = "Medicamentos para dor")
    private String descricao;
}





