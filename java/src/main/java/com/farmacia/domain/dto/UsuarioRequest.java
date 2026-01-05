package com.farmacia.domain.dto;

import com.farmacia.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(
    description = "Dados para criação/atualização de usuário",
    example = "{\"nome\":\"João Silva\",\"email\":\"joao@farmacia.com\",\"password\":\"senha123\",\"role\":\"ADMIN\"}"
)
public class UsuarioRequest {

    @NotBlank(message = "O campo Nome é obrigatório, por favor preencha.")
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String nome;

    @NotBlank(message = "O campo Email é obrigatório, por favor preencha.")
    @Email(message = "O campo Email contém um valor inválido, por favor alterar.")
    @Schema(description = "Email do usuário", example = "joao@farmacia.com")
    private String email;

    @Size(min = 6, message = "O campo Senha deve ter no mínimo 6 caracteres, por favor alterar.")
    @Schema(description = "Senha do usuário (obrigatória na criação, opcional na atualização, mínimo 6 caracteres)", example = "senha123")
    private String password;

    @Schema(description = "Papel do usuário no sistema (obrigatório na criação)", example = "ADMIN", allowableValues = {"ADMIN", "VENDEDOR"})
    private Role role;

    @Schema(description = "URL do avatar do usuário", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
}




