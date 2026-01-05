package com.farmacia.controller;

import com.farmacia.domain.dto.MensagemResponse;
import com.farmacia.domain.dto.VendaRequest;
import com.farmacia.domain.dto.VendaResponse;
import com.farmacia.service.VendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendas")
@Tag(name = "Vendas", description = "Endpoints para gerenciamento de vendas")
@SecurityRequirement(name = "Bearer Authentication")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @PostMapping
    @Operation(
        summary = "Criar venda", 
        description = "Cria uma nova venda (ADMIN e VENDEDOR).\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. Clique em 'Try it out'\n" +
                      "2. No campo 'Request body', cole o JSON abaixo:\n" +
                      "```json\n" +
                      "{\"clienteId\":\"550e8400-e29b-41d4-a716-446655440000\",\"itens\":[{\"medicamentoId\":\"550e8400-e29b-41d4-a716-446655440001\",\"quantidade\":2}]}\n" +
                      "```\n\n" +
                      "**FORMATO DO JSON:**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"clienteId\": \"550e8400-e29b-41d4-a716-446655440000\",\n" +
                      "  \"itens\": [\n" +
                      "    {\n" +
                      "      \"medicamentoId\": \"550e8400-e29b-41d4-a716-446655440001\",\n" +
                      "      \"quantidade\": 2\n" +
                      "    }\n" +
                      "  ]\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- clienteId: UUID (obrigatório, ID de um cliente existente)\n" +
                      "- itens: Array (obrigatório, pelo menos 1 item)\n" +
                      "  - medicamentoId: UUID (obrigatório, ID de um medicamento existente e ativo)\n" +
                      "  - quantidade: Integer (obrigatório, >= 1)\n\n" +
                      "**Validações:**\n" +
                      "- Cliente deve ter mais de 18 anos\n" +
                      "- Medicamentos devem estar ativos e dentro da validade\n" +
                      "- Deve haver estoque suficiente para cada medicamento"
    )
    public ResponseEntity<VendaResponse> create(
            @Parameter(
                description = "Dados da venda",
                examples = @ExampleObject(
                    name = "Exemplo de Venda",
                    value = "{\"clienteId\":\"550e8400-e29b-41d4-a716-446655440000\",\"itens\":[{\"medicamentoId\":\"550e8400-e29b-41d4-a716-446655440001\",\"quantidade\":2}]}",
                    summary = "Exemplo completo"
                )
            )
            @Valid @RequestBody VendaRequest request) {
        VendaResponse response = vendaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar vendas", description = "Retorna todas as vendas (ADMIN e VENDEDOR)")
    public ResponseEntity<List<VendaResponse>> findAll() {
        List<VendaResponse> response = vendaService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar venda por ID", description = "Retorna uma venda específica (ADMIN e VENDEDOR)")
    public ResponseEntity<VendaResponse> findById(@PathVariable UUID id) {
        VendaResponse response = vendaService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Buscar vendas por cliente", description = "Retorna todas as vendas de um cliente específico (ADMIN e VENDEDOR)")
    public ResponseEntity<List<VendaResponse>> findByClienteId(@PathVariable UUID clienteId) {
        List<VendaResponse> response = vendaService.findByClienteId(clienteId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar venda", 
               description = "Cancela uma venda concluída e estorna o estoque dos medicamentos. Retorna mensagem de confirmação. (ADMIN e VENDEDOR)")
    public ResponseEntity<MensagemResponse> cancelar(@PathVariable UUID id) {
        String mensagem = vendaService.cancelar(id);
        MensagemResponse response = new MensagemResponse(mensagem, id.toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancelada")
    @Operation(
        summary = "Criar venda cancelada", 
        description = "Cria uma venda com status CANCELADA (sem atualizar estoque). Usado quando o usuário cancela uma venda antes de finalizá-la. (ADMIN e VENDEDOR)\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. Clique em 'Try it out'\n" +
                      "2. No campo 'Request body', cole o JSON abaixo:\n" +
                      "```json\n" +
                      "{\"clienteId\":\"550e8400-e29b-41d4-a716-446655440000\",\"itens\":[{\"medicamentoId\":\"550e8400-e29b-41d4-a716-446655440001\",\"quantidade\":2}]}\n" +
                      "```\n\n" +
                      "**FORMATO DO JSON:**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"clienteId\": \"550e8400-e29b-41d4-a716-446655440000\",\n" +
                      "  \"itens\": [\n" +
                      "    {\n" +
                      "      \"medicamentoId\": \"550e8400-e29b-41d4-a716-446655440001\",\n" +
                      "      \"quantidade\": 2\n" +
                      "    }\n" +
                      "  ]\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Nota:** Esta venda será criada com status CANCELADA e não afetará o estoque."
    )
    public ResponseEntity<VendaResponse> createCancelada(
            @Parameter(
                description = "Dados da venda cancelada",
                examples = @ExampleObject(
                    name = "Exemplo de Venda Cancelada",
                    value = "{\"clienteId\":\"550e8400-e29b-41d4-a716-446655440000\",\"itens\":[{\"medicamentoId\":\"550e8400-e29b-41d4-a716-446655440001\",\"quantidade\":2}]}",
                    summary = "Exemplo completo"
                )
            )
            @Valid @RequestBody VendaRequest request) {
        VendaResponse response = vendaService.createCancelada(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}



