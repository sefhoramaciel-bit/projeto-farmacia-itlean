package com.farmacia.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(
    description = "Dados para criação/atualização de cliente",
    example = "{\"nome\":\"Maria Santos\",\"cpf\":\"123.456.789-00\",\"telefone\":\"(11) 98765-4321\",\"email\":\"maria@email.com\",\"endereco\":\"Rua das Flores, 123\",\"dataNascimento\":\"1990-05-15\"}"
)
public class ClienteRequest {

    @NotBlank(message = "O campo Nome é obrigatório, por favor preencha.")
    @Schema(description = "Nome completo do cliente", example = "Maria Santos")
    private String nome;

    @NotBlank(message = "O campo CPF é obrigatório, por favor preencha.")
    @Schema(description = "CPF do cliente", example = "123.456.789-00")
    private String cpf;

    @Schema(description = "Telefone do cliente", example = "(11) 98765-4321")
    private String telefone;

    @NotBlank(message = "O campo Email é obrigatório, por favor preencha.")
    @Email(message = "O campo Email contém um valor inválido, por favor alterar.")
    @Schema(description = "Email do cliente", example = "maria@email.com")
    private String email;

    @Schema(description = "Endereço do cliente", example = "Rua das Flores, 123")
    private String endereco;

    @NotNull(message = "O campo Data de Nascimento é obrigatório, por favor preencha.")
    @Past(message = "O campo Data de Nascimento deve ser uma data passada, por favor alterar.")
    @Schema(description = "Data de nascimento (obrigatório, formato: YYYY-MM-DD, deve ser passada)", example = "1990-05-15")
    private LocalDate dataNascimento;
}




