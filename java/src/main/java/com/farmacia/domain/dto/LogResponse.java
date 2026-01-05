package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de log de auditoria")
public class LogResponse {

    @Schema(description = "ID do log", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Tipo de operação", example = "CREATE", allowableValues = {"CREATE", "UPDATE", "DELETE", "LOGIN"})
    private String tipoOperacao;

    @Schema(description = "Tipo de entidade", example = "MEDICAMENTO", allowableValues = {"USUARIO", "MEDICAMENTO", "CATEGORIA", "CLIENTE", "VENDA", "ESTOQUE", "LOGIN"})
    private String tipoEntidade;

    @Schema(description = "ID da entidade afetada", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID entidadeId;

    @Schema(description = "Descrição da operação", example = "Medicamento criado: Dipirona 500mg")
    private String descricao;

    @Schema(description = "Detalhes adicionais", example = "{\"nome\": \"Dipirona 500mg\", \"preco\": 15.50}")
    private String detalhes;

    @Schema(description = "ID do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID usuarioId;

    @Schema(description = "Nome do usuário", example = "João Silva")
    private String usuarioNome;

    @Schema(description = "Email do usuário", example = "joao@farmacia.com")
    private String usuarioEmail;

    @Schema(description = "Data e hora da operação (formato: dd/MM/yyyy HH:mm:ss)", example = "01/01/2024 10:30:00")
    private LocalDateTime dataHora;
}






