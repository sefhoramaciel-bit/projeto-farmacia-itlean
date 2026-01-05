package com.farmacia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmacia.domain.dto.MedicamentoRequest;
import com.farmacia.domain.dto.MedicamentoResponse;
import com.farmacia.domain.dto.MensagemResponse;
import com.farmacia.service.MedicamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medicamentos")
@Tag(name = "Medicamentos", description = "Endpoints para gerenciamento de medicamentos")
@SecurityRequirement(name = "Bearer Authentication")
public class MedicamentoController {

    @Autowired
    private MedicamentoService medicamentoService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Criar medicamento", 
        description = "Cria um novo medicamento com imagens (apenas ADMIN).\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. **Campo 'medicamento' (obrigatório):**\n" +
                      "   - Cole o JSON abaixo no campo de texto:\n" +
                      "   ```json\n" +
                      "   {\"nome\":\"Dipirona 500mg\",\"descricao\":\"Medicamento analgésico e antitérmico\",\"preco\":15.50,\"quantidadeEstoque\":100,\"validade\":\"2025-12-31\",\"ativo\":true,\"categoriaId\":\"550e8400-e29b-41d4-a716-446655440000\"}\n" +
                      "   ```\n\n" +
                      "2. **Campo 'files' (obrigatório):**\n" +
                      "   - Clique em 'Add string item' ou use o botão de upload de arquivo\n" +
                      "   - Selecione 1 a 3 imagens (JPG, PNG ou WebP)\n" +
                      "   - Tamanho máximo por imagem: 5MB\n\n" +
                      "**FORMATO DO JSON (campo 'medicamento'):**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"nome\": \"Dipirona 500mg\",\n" +
                      "  \"descricao\": \"Medicamento analgésico e antitérmico\",\n" +
                      "  \"preco\": 15.50,\n" +
                      "  \"quantidadeEstoque\": 100,\n" +
                      "  \"validade\": \"2025-12-31\",\n" +
                      "  \"ativo\": true,\n" +
                      "  \"categoriaId\": \"550e8400-e29b-41d4-a716-446655440000\"\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- nome: String (obrigatório, único)\n" +
                      "- preco: BigDecimal (obrigatório, > 0)\n" +
                      "- quantidadeEstoque: Integer (obrigatório, >= 0)\n" +
                      "- validade: LocalDate (obrigatório, formato: YYYY-MM-DD, deve ser futura)\n" +
                      "- categoriaId: UUID (obrigatório)\n\n" +
                      "**Campos opcionais:**\n" +
                      "- descricao: String\n" +
                      "- ativo: Boolean (padrão: true)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicamentoResponse> create(
            @Parameter(
                name = "medicamento",
                description = "JSON com os dados do medicamento (obrigatório). Exemplo:",
                required = true,
                examples = @ExampleObject(
                    name = "Exemplo de Medicamento",
                    value = "{\"nome\":\"Dipirona 500mg\",\"descricao\":\"Medicamento analgésico e antitérmico\",\"preco\":15.50,\"quantidadeEstoque\":100,\"validade\":\"2025-12-31\",\"ativo\":true,\"categoriaId\":\"550e8400-e29b-41d4-a716-446655440000\"}",
                    summary = "Exemplo completo"
                )
            )
            @RequestPart("medicamento") String medicamentoJson,
            @Parameter(
                name = "files",
                description = "Arquivos de imagem do medicamento (1 a 3 imagens). Formatos: JPG, PNG, WebP. Tamanho máximo: 5MB por imagem."
            )
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws Exception {
        MedicamentoRequest request = objectMapper.readValue(medicamentoJson, MedicamentoRequest.class);
        MedicamentoResponse response = medicamentoService.create(request, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar medicamentos", description = "Retorna todos os medicamentos (ADMIN e VENDEDOR)")
    public ResponseEntity<List<MedicamentoResponse>> findAll() {
        List<MedicamentoResponse> response = medicamentoService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar medicamentos ativos", description = "Retorna apenas medicamentos ativos (ADMIN e VENDEDOR)")
    public ResponseEntity<List<MedicamentoResponse>> findActive() {
        List<MedicamentoResponse> response = medicamentoService.findActive();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar medicamento por ID", description = "Retorna um medicamento específico (ADMIN e VENDEDOR)")
    public ResponseEntity<MedicamentoResponse> findById(@PathVariable UUID id) {
        MedicamentoResponse response = medicamentoService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Atualizar medicamento", 
        description = "Atualiza um medicamento existente com imagens (apenas ADMIN).\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. **Campo 'medicamento' (obrigatório):**\n" +
                      "   - Cole o JSON abaixo no campo de texto:\n" +
                      "   ```json\n" +
                      "   {\"nome\":\"Dipirona 500mg\",\"descricao\":\"Medicamento analgésico e antitérmico\",\"preco\":15.50,\"quantidadeEstoque\":100,\"validade\":\"2025-12-31\",\"ativo\":true,\"categoriaId\":\"550e8400-e29b-41d4-a716-446655440000\"}\n" +
                      "   ```\n\n" +
                      "2. **Campo 'files' (opcional):**\n" +
                      "   - Se não enviar imagens, as imagens existentes serão mantidas\n" +
                      "   - Se enviar imagens, as antigas serão substituídas\n" +
                      "   - Clique em 'Add string item' ou use o botão de upload de arquivo\n" +
                      "   - Selecione 1 a 3 imagens (JPG, PNG ou WebP)\n" +
                      "   - Tamanho máximo por imagem: 5MB\n\n" +
                      "**FORMATO DO JSON (campo 'medicamento'):**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"nome\": \"Dipirona 500mg\",\n" +
                      "  \"descricao\": \"Medicamento analgésico e antitérmico\",\n" +
                      "  \"preco\": 15.50,\n" +
                      "  \"quantidadeEstoque\": 100,\n" +
                      "  \"validade\": \"2025-12-31\",\n" +
                      "  \"ativo\": true,\n" +
                      "  \"categoriaId\": \"550e8400-e29b-41d4-a716-446655440000\"\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- nome: String (obrigatório, único)\n" +
                      "- preco: BigDecimal (obrigatório, > 0)\n" +
                      "- quantidadeEstoque: Integer (obrigatório, >= 0)\n" +
                      "- validade: LocalDate (obrigatório, formato: YYYY-MM-DD, deve ser futura)\n" +
                      "- categoriaId: UUID (obrigatório)\n\n" +
                      "**Campos opcionais:**\n" +
                      "- descricao: String\n" +
                      "- ativo: Boolean"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicamentoResponse> update(
            @PathVariable UUID id,
            @Parameter(
                name = "medicamento",
                description = "JSON com os dados do medicamento (obrigatório). Exemplo:",
                required = true,
                examples = @ExampleObject(
                    name = "Exemplo de Medicamento",
                    value = "{\"nome\":\"Dipirona 500mg\",\"descricao\":\"Medicamento analgésico e antitérmico\",\"preco\":15.50,\"quantidadeEstoque\":100,\"validade\":\"2025-12-31\",\"ativo\":true,\"categoriaId\":\"550e8400-e29b-41d4-a716-446655440000\"}",
                    summary = "Exemplo completo"
                )
            )
            @RequestPart("medicamento") String medicamentoJson,
            @Parameter(
                name = "files",
                description = "Arquivos de imagem do medicamento (opcional, 1 a 3 imagens). Se não enviar, mantém as imagens existentes. Formatos: JPG, PNG, WebP. Tamanho máximo: 5MB por imagem."
            )
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws Exception {
        MedicamentoRequest request = objectMapper.readValue(medicamentoJson, MedicamentoRequest.class);
        MedicamentoResponse response = medicamentoService.update(id, request, files);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do medicamento", description = "Ativa ou inativa um medicamento (apenas ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicamentoResponse> updateStatus(@PathVariable UUID id, @RequestParam Boolean ativo) {
        MedicamentoResponse response = medicamentoService.updateStatus(id, ativo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar medicamento", description = "Deleta um medicamento. Retorna mensagem de confirmação. (apenas ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensagemResponse> delete(@PathVariable UUID id) {
        String mensagem = medicamentoService.delete(id);
        MensagemResponse response = new MensagemResponse(mensagem, id.toString());
        return ResponseEntity.ok(response);
    }
}
