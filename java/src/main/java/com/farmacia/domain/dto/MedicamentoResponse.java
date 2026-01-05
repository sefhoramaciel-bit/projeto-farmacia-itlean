package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados do medicamento")
public class MedicamentoResponse {

    @Schema(description = "ID do medicamento", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Nome do medicamento", example = "Dipirona 500mg")
    private String nome;

    @Schema(description = "Descrição do medicamento", example = "Medicamento analgésico e antitérmico")
    private String descricao;

    @Schema(description = "Preço unitário", example = "15.50")
    private BigDecimal preco;

    @Schema(description = "Quantidade em estoque", example = "100")
    private Integer quantidadeEstoque;

    @Schema(description = "Data de validade (formato: dd/MM/yyyy)", example = "31/12/2025")
    private LocalDate validade;

    @Schema(description = "Status ativo", example = "true")
    private Boolean ativo;

    @Schema(description = "Categoria do medicamento")
    private CategoriaResponse categoria;

    @Schema(description = "URLs das imagens do medicamento", example = "[\"/uploads/medicamentos/imagem1.jpg\"]")
    private List<String> imagens;

    @Schema(description = "Data de criação (formato: dd/MM/yyyy HH:mm:ss)", example = "01/01/2024 10:30:00")
    private LocalDateTime createdAt;
}




