package com.farmacia.controller;

import com.farmacia.domain.dto.CategoriaRequest;
import com.farmacia.domain.dto.CategoriaResponse;
import com.farmacia.domain.dto.MensagemResponse;
import com.farmacia.service.CategoriaService;
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
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Criar categoria", 
        description = "Cria uma nova categoria (apenas ADMIN).\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. Clique em 'Try it out'\n" +
                      "2. No campo 'Request body', cole o JSON abaixo:\n" +
                      "```json\n" +
                      "{\"nome\":\"Analgésicos\",\"descricao\":\"Medicamentos para dor\"}\n" +
                      "```\n\n" +
                      "**FORMATO DO JSON:**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"nome\": \"Analgésicos\",\n" +
                      "  \"descricao\": \"Medicamentos para dor\"\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- nome: String (obrigatório, único)\n\n" +
                      "**Campos opcionais:**\n" +
                      "- descricao: String"
    )
    public ResponseEntity<CategoriaResponse> create(
            @Parameter(
                description = "Dados da categoria",
                examples = @ExampleObject(
                    name = "Exemplo de Categoria",
                    value = "{\"nome\":\"Analgésicos\",\"descricao\":\"Medicamentos para dor\"}",
                    summary = "Exemplo completo"
                )
            )
            @Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = categoriaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar categorias", description = "Retorna todas as categorias (ADMIN e VENDEDOR)")
    public ResponseEntity<List<CategoriaResponse>> findAll() {
        List<CategoriaResponse> response = categoriaService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID", description = "Retorna uma categoria específica (ADMIN e VENDEDOR)")
    public ResponseEntity<CategoriaResponse> findById(@PathVariable UUID id) {
        CategoriaResponse response = categoriaService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Atualizar categoria", 
        description = "Atualiza uma categoria existente (apenas ADMIN).\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. Clique em 'Try it out'\n" +
                      "2. Informe o ID da categoria no campo 'id'\n" +
                      "3. No campo 'Request body', cole o JSON abaixo:\n" +
                      "```json\n" +
                      "{\"nome\":\"Analgésicos\",\"descricao\":\"Medicamentos para dor\"}\n" +
                      "```\n\n" +
                      "**FORMATO DO JSON:**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"nome\": \"Analgésicos\",\n" +
                      "  \"descricao\": \"Medicamentos para dor\"\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- nome: String (obrigatório, único)\n\n" +
                      "**Campos opcionais:**\n" +
                      "- descricao: String"
    )
    public ResponseEntity<CategoriaResponse> update(
            @PathVariable UUID id,
            @Parameter(
                description = "Dados da categoria",
                examples = @ExampleObject(
                    name = "Exemplo de Categoria",
                    value = "{\"nome\":\"Analgésicos\",\"descricao\":\"Medicamentos para dor\"}",
                    summary = "Exemplo completo"
                )
            )
            @Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = categoriaService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar categoria", description = "Deleta uma categoria. Retorna mensagem de confirmação ou erro se houver medicamentos vinculados. (apenas ADMIN)")
    public ResponseEntity<MensagemResponse> delete(@PathVariable UUID id) {
        String mensagem = categoriaService.delete(id);
        MensagemResponse response = new MensagemResponse(mensagem, id.toString());
        return ResponseEntity.ok(response);
    }
}



