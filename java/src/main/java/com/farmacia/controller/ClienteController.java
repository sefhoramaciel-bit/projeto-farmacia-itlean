package com.farmacia.controller;

import com.farmacia.domain.dto.ClienteRequest;
import com.farmacia.domain.dto.ClienteResponse;
import com.farmacia.domain.dto.MensagemResponse;
import com.farmacia.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes")
@SecurityRequirement(name = "Bearer Authentication")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Criar cliente", 
        description = "Cria um novo cliente (apenas ADMIN).\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. Clique em 'Try it out'\n" +
                      "2. No campo 'Request body', cole o JSON abaixo:\n" +
                      "```json\n" +
                      "{\"nome\":\"Maria Santos\",\"cpf\":\"123.456.789-00\",\"telefone\":\"(11) 98765-4321\",\"email\":\"maria@email.com\",\"endereco\":\"Rua das Flores, 123\",\"dataNascimento\":\"1990-05-15\"}\n" +
                      "```\n\n" +
                      "**FORMATO DO JSON:**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"nome\": \"Maria Santos\",\n" +
                      "  \"cpf\": \"123.456.789-00\",\n" +
                      "  \"telefone\": \"(11) 98765-4321\",\n" +
                      "  \"email\": \"maria@email.com\",\n" +
                      "  \"endereco\": \"Rua das Flores, 123\",\n" +
                      "  \"dataNascimento\": \"1990-05-15\"\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- nome: String (obrigatório)\n" +
                      "- cpf: String (obrigatório, único, formato: XXX.XXX.XXX-XX)\n" +
                      "- email: String (obrigatório, válido, único)\n" +
                      "- dataNascimento: LocalDate (obrigatório, formato: YYYY-MM-DD, deve ser passada)\n\n" +
                      "**Campos opcionais:**\n" +
                      "- telefone: String\n" +
                      "- endereco: String"
    )
    public ResponseEntity<ClienteResponse> create(
            @Parameter(
                description = "Dados do cliente",
                examples = @ExampleObject(
                    name = "Exemplo de Cliente",
                    value = "{\"nome\":\"Maria Santos\",\"cpf\":\"123.456.789-00\",\"telefone\":\"(11) 98765-4321\",\"email\":\"maria@email.com\",\"endereco\":\"Rua das Flores, 123\",\"dataNascimento\":\"1990-05-15\"}",
                    summary = "Exemplo completo"
                )
            )
            @Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna todos os clientes (ADMIN e VENDEDOR)")
    public ResponseEntity<List<ClienteResponse>> findAll() {
        List<ClienteResponse> response = clienteService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna um cliente específico (ADMIN e VENDEDOR)")
    public ResponseEntity<ClienteResponse> findById(@PathVariable UUID id) {
        ClienteResponse response = clienteService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Atualizar cliente", 
        description = "Atualiza um cliente existente (apenas ADMIN).\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. Clique em 'Try it out'\n" +
                      "2. Informe o ID do cliente no campo 'id'\n" +
                      "3. No campo 'Request body', cole o JSON abaixo:\n" +
                      "```json\n" +
                      "{\"nome\":\"Maria Santos\",\"cpf\":\"123.456.789-00\",\"telefone\":\"(11) 98765-4321\",\"email\":\"maria@email.com\",\"endereco\":\"Rua das Flores, 123\",\"dataNascimento\":\"1990-05-15\"}\n" +
                      "```\n\n" +
                      "**FORMATO DO JSON:**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"nome\": \"Maria Santos\",\n" +
                      "  \"cpf\": \"123.456.789-00\",\n" +
                      "  \"telefone\": \"(11) 98765-4321\",\n" +
                      "  \"email\": \"maria@email.com\",\n" +
                      "  \"endereco\": \"Rua das Flores, 123\",\n" +
                      "  \"dataNascimento\": \"1990-05-15\"\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- nome: String (obrigatório)\n" +
                      "- cpf: String (obrigatório, único, formato: XXX.XXX.XXX-XX)\n" +
                      "- email: String (obrigatório, válido, único)\n" +
                      "- dataNascimento: LocalDate (obrigatório, formato: YYYY-MM-DD, deve ser passada)\n\n" +
                      "**Campos opcionais:**\n" +
                      "- telefone: String\n" +
                      "- endereco: String"
    )
    public ResponseEntity<ClienteResponse> update(
            @PathVariable UUID id,
            @Parameter(
                description = "Dados do cliente",
                examples = @ExampleObject(
                    name = "Exemplo de Cliente",
                    value = "{\"nome\":\"Maria Santos\",\"cpf\":\"123.456.789-00\",\"telefone\":\"(11) 98765-4321\",\"email\":\"maria@email.com\",\"endereco\":\"Rua das Flores, 123\",\"dataNascimento\":\"1990-05-15\"}",
                    summary = "Exemplo completo"
                )
            )
            @Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar cliente", description = "Deleta um cliente. Retorna mensagem de confirmação. (apenas ADMIN)")
    public ResponseEntity<MensagemResponse> delete(@PathVariable UUID id) {
        String mensagem = clienteService.delete(id);
        MensagemResponse response = new MensagemResponse(mensagem, id.toString());
        return ResponseEntity.ok(response);
    }
}



