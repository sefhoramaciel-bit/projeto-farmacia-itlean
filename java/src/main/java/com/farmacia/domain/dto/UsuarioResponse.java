package com.farmacia.domain.dto;

import com.farmacia.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados do usuário")
public class UsuarioResponse {

    @Schema(description = "ID do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Nome completo", example = "João Silva")
    private String nome;

    @Schema(description = "Email", example = "joao@farmacia.com")
    private String email;

    @Schema(description = "Papel do usuário", example = "ADMIN")
    private Role role;

    @Schema(description = "URL do avatar", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Data de criação (formato: dd/MM/yyyy HH:mm:ss)", example = "01/01/2024 10:30:00")
    private LocalDateTime createdAt;
}




