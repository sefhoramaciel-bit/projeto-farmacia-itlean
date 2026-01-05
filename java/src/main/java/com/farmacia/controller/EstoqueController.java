package com.farmacia.controller;

import com.farmacia.domain.dto.EstoqueOperacaoResponse;
import com.farmacia.domain.dto.EstoqueRequest;
import com.farmacia.domain.dto.EstoqueResponse;
import com.farmacia.domain.dto.EstoqueSaidaRequest;
import com.farmacia.domain.entity.MovimentacaoEstoque;
import com.farmacia.service.EstoqueService;
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
@RequestMapping("/api/estoque")
@Tag(name = "Estoque", description = "Endpoints para gerenciamento de estoque")
@SecurityRequirement(name = "Bearer Authentication")
public class EstoqueController {

    @Autowired
    private EstoqueService estoqueService;

    @PostMapping("/entrada")
    @Operation(
        summary = "Entrada de estoque", 
        description = "Adiciona quantidade ao estoque de um medicamento. Aumenta o estoque e registra a movimentação. (ADMIN e VENDEDOR)\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. Clique em 'Try it out'\n" +
                      "2. No campo 'Request body', cole o JSON abaixo:\n" +
                      "```json\n" +
                      "{\"medicamentoId\":\"550e8400-e29b-41d4-a716-446655440000\",\"quantidade\":10,\"motivo\":\"Entrada de estoque\"}\n" +
                      "```\n\n" +
                      "**FORMATO DO JSON:**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"medicamentoId\": \"550e8400-e29b-41d4-a716-446655440000\",\n" +
                      "  \"quantidade\": 10,\n" +
                      "  \"motivo\": \"Entrada de estoque\"\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- medicamentoId: UUID (obrigatório, ID de um medicamento existente)\n" +
                      "- quantidade: Integer (obrigatório, > 0)\n\n" +
                      "**Campos opcionais:**\n" +
                      "- motivo: String (padrão: 'Entrada de estoque')"
    )
    public ResponseEntity<?> entrada(
            @Parameter(
                description = "Dados da entrada de estoque",
                examples = @ExampleObject(
                    name = "Exemplo de Entrada",
                    value = "{\"medicamentoId\":\"550e8400-e29b-41d4-a716-446655440000\",\"quantidade\":10,\"motivo\":\"Entrada de estoque\"}",
                    summary = "Exemplo completo"
                )
            )
            @Valid @RequestBody EstoqueRequest request) {
        try {
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            System.out.println("📦 EstoqueController.entrada() - INÍCIO");
            System.out.println("📦 Request recebido: " + request);
            System.out.println("📦 Medicamento ID: " + request.getMedicamentoId());
            System.out.println("📦 Quantidade: " + request.getQuantidade());
            System.out.println("📦 Motivo: " + request.getMotivo());
            
            if (request.getMedicamentoId() == null) {
                System.out.println("📦 ❌ ERRO: Medicamento ID é nulo!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID do medicamento é obrigatório");
            }
            
            if (request.getQuantidade() == null || request.getQuantidade() <= 0) {
                System.out.println("📦 ❌ ERRO: Quantidade inválida!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantidade deve ser maior que zero");
            }
            
            String motivo = request.getMotivo() != null ? request.getMotivo() : "Entrada de estoque";
            EstoqueOperacaoResponse response = estoqueService.adicionarEstoque(request.getMedicamentoId(), request.getQuantidade(), motivo);
            System.out.println("📦 EstoqueController.entrada() - FIM - Sucesso");
            System.out.println("📦 Estoque Atual: " + response.getQuantidadeEstoqueAtual());
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            System.out.println("📦 ❌ ERRO em entrada(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar entrada de estoque: " + e.getMessage());
        }
    }

    @PostMapping("/saida")
    @Operation(
        summary = "Saída de estoque", 
        description = "Remove quantidade do estoque de um medicamento. Diminui o estoque e registra a movimentação. Não permite saída maior que o estoque disponível. (ADMIN e VENDEDOR)\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. Clique em 'Try it out'\n" +
                      "2. No campo 'Request body', cole o JSON abaixo:\n" +
                      "```json\n" +
                      "{\"medicamentoId\":\"550e8400-e29b-41d4-a716-446655440000\",\"quantidade\":10,\"motivo\":\"Saída de estoque\",\"tipoOperacao\":\"SAIDA\"}\n" +
                      "```\n\n" +
                      "**FORMATO DO JSON:**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"medicamentoId\": \"550e8400-e29b-41d4-a716-446655440000\",\n" +
                      "  \"quantidade\": 10,\n" +
                      "  \"motivo\": \"Saída de estoque\",\n" +
                      "  \"tipoOperacao\": \"SAIDA\"\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- medicamentoId: UUID (obrigatório, ID de um medicamento existente)\n" +
                      "- quantidade: Integer (obrigatório, > 0, não pode exceder o estoque disponível)\n\n" +
                      "**Campos opcionais:**\n" +
                      "- motivo: String (padrão: 'Saída de estoque')\n" +
                      "- tipoOperacao: String (padrão: 'SAIDA')"
    )
    public ResponseEntity<?> saida(
            @Parameter(
                description = "Dados da saída de estoque",
                examples = @ExampleObject(
                    name = "Exemplo de Saída",
                    value = "{\"medicamentoId\":\"550e8400-e29b-41d4-a716-446655440000\",\"quantidade\":10,\"motivo\":\"Saída de estoque\",\"tipoOperacao\":\"SAIDA\"}",
                    summary = "Exemplo completo"
                )
            )
            @Valid @RequestBody EstoqueSaidaRequest request) {
        try {
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            System.out.println("📦 EstoqueController.saida() - INÍCIO");
            System.out.println("📦 Request recebido: " + request);
            System.out.println("📦 Medicamento ID: " + request.getMedicamentoId());
            System.out.println("📦 Quantidade: " + request.getQuantidade());
            System.out.println("📦 Motivo: " + request.getMotivo());
            System.out.println("📦 Tipo Operação: " + request.getTipoOperacao());
            
            if (request.getMedicamentoId() == null) {
                System.out.println("📦 ❌ ERRO: Medicamento ID é nulo!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID do medicamento é obrigatório");
            }
            
            if (request.getQuantidade() == null || request.getQuantidade() <= 0) {
                System.out.println("📦 ❌ ERRO: Quantidade inválida!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantidade deve ser maior que zero");
            }
            
            String motivo = request.getMotivo() != null ? request.getMotivo() : "Saída de estoque";
            EstoqueOperacaoResponse response = estoqueService.removerEstoque(request.getMedicamentoId(), request.getQuantidade(), motivo);
            System.out.println("📦 EstoqueController.saida() - FIM - Sucesso");
            System.out.println("📦 Estoque Atual: " + response.getQuantidadeEstoqueAtual());
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            System.out.println("📦 ❌ ERRO em saida(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar saída de estoque: " + e.getMessage());
        }
    }

    @GetMapping("/{medicamentoId}")
    @Operation(summary = "Consultar estoque", 
               description = "Retorna informações de estoque de um medicamento específico (ADMIN e VENDEDOR)")
    public ResponseEntity<EstoqueResponse> getEstoque(@PathVariable UUID medicamentoId) {
        EstoqueResponse estoque = estoqueService.getEstoqueByMedicamento(medicamentoId);
        return ResponseEntity.ok(estoque);
    }

    @GetMapping("/{medicamentoId}/movimentacoes")
    @Operation(summary = "Listar movimentações", 
               description = "Retorna todas as movimentações de um medicamento (ADMIN e VENDEDOR)")
    public ResponseEntity<List<MovimentacaoEstoque>> getMovimentacoes(@PathVariable UUID medicamentoId) {
        List<MovimentacaoEstoque> movimentacoes = estoqueService.getMovimentacoesByMedicamento(medicamentoId);
        return ResponseEntity.ok(movimentacoes);
    }
}



