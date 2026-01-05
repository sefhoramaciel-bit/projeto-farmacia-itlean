package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados do cliente")
public class ClienteResponse {

    @Schema(description = "ID do cliente", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Nome completo", example = "Maria Santos")
    private String nome;

    @Schema(description = "CPF", example = "123.456.789-00")
    private String cpf;

    @Schema(description = "Telefone", example = "(11) 98765-4321")
    private String telefone;

    @Schema(description = "Email", example = "maria@email.com")
    private String email;

    @Schema(description = "Endereço", example = "Rua das Flores, 123")
    private String endereco;

    @Schema(description = "Data de nascimento (formato: dd/MM/yyyy)", example = "15/05/1990")
    private LocalDate dataNascimento;

    @Schema(description = "Data de criação (formato: dd/MM/yyyy HH:mm:ss)", example = "01/01/2024 10:30:00")
    private LocalDateTime createdAt;
}




