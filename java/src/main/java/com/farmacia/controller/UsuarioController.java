package com.farmacia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmacia.domain.dto.MensagemResponse;
import com.farmacia.domain.dto.UsuarioRequest;
import com.farmacia.domain.dto.UsuarioResponse;
import com.farmacia.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
@SecurityRequirement(name = "Bearer Authentication")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Criar usuário", 
        description = "Cria um novo usuário com avatar opcional (apenas ADMIN).\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. Clique em 'Try it out'\n" +
                      "2. **Campo 'usuario' (obrigatório):**\n" +
                      "   - Cole o JSON abaixo no campo de texto:\n" +
                      "   ```json\n" +
                      "   {\"nome\":\"João Silva\",\"email\":\"joao@farmacia.com\",\"password\":\"senha123\",\"role\":\"ADMIN\"}\n" +
                      "   ```\n\n" +
                      "3. **Campo 'avatar' (opcional):**\n" +
                      "   - Use o botão de upload para selecionar uma imagem\n" +
                      "   - Formatos aceitos: JPG, PNG, WebP\n" +
                      "   - Tamanho máximo: 5MB\n\n" +
                      "**FORMATO DO JSON (campo 'usuario'):**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"nome\": \"João Silva\",\n" +
                      "  \"email\": \"joao@farmacia.com\",\n" +
                      "  \"password\": \"senha123\",\n" +
                      "  \"role\": \"ADMIN\"\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- nome: String (obrigatório)\n" +
                      "- email: String (obrigatório, válido, único)\n" +
                      "- password: String (obrigatório na criação)\n" +
                      "- role: String (obrigatório, valores: ADMIN ou VENDEDOR)\n\n" +
                      "**Campos opcionais:**\n" +
                      "- avatarUrl: String (ou envie arquivo no campo 'avatar')"
    )
    public ResponseEntity<UsuarioResponse> create(
            @Parameter(
                name = "usuario",
                description = "JSON com os dados do usuário (obrigatório). Exemplo:",
                required = true,
                examples = @ExampleObject(
                    name = "Exemplo de Usuário",
                    value = "{\"nome\":\"João Silva\",\"email\":\"joao@farmacia.com\",\"password\":\"senha123\",\"role\":\"ADMIN\"}",
                    summary = "Exemplo completo"
                )
            )
            @RequestPart("usuario") String usuarioJson,
            @Parameter(
                name = "avatar",
                description = "Arquivo de imagem do avatar (opcional). Formatos: JPG, PNG, WebP. Tamanho máximo: 5MB."
            )
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) throws Exception {
        UsuarioRequest request = objectMapper.readValue(usuarioJson, UsuarioRequest.class);
        UsuarioResponse response = usuarioService.create(request, avatarFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuários", description = "Retorna todos os usuários (apenas ADMIN)")
    public ResponseEntity<List<UsuarioResponse>> findAll() {
        List<UsuarioResponse> response = usuarioService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna um usuário específico (apenas ADMIN)")
    public ResponseEntity<UsuarioResponse> findById(@PathVariable UUID id) {
        UsuarioResponse response = usuarioService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Atualizar usuário", 
        description = "Atualiza um usuário existente com avatar opcional (apenas ADMIN).\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. Clique em 'Try it out'\n" +
                      "2. Informe o ID do usuário no campo 'id'\n" +
                      "3. **Campo 'usuario' (obrigatório):**\n" +
                      "   - Cole o JSON abaixo no campo de texto:\n" +
                      "   ```json\n" +
                      "   {\"nome\":\"João Silva\",\"email\":\"joao@farmacia.com\",\"password\":\"senha123\",\"role\":\"ADMIN\"}\n" +
                      "   ```\n\n" +
                      "4. **Campo 'avatar' (opcional):**\n" +
                      "   - Use o botão de upload para selecionar uma imagem\n" +
                      "   - Se não enviar nada, mantém o avatar atual\n" +
                      "   - Formatos aceitos: JPG, PNG, WebP\n" +
                      "   - Tamanho máximo: 5MB\n\n" +
                      "**FORMATO DO JSON (campo 'usuario'):**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"nome\": \"João Silva\",\n" +
                      "  \"email\": \"joao@farmacia.com\",\n" +
                      "  \"password\": \"senha123\",\n" +
                      "  \"role\": \"ADMIN\"\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- nome: String (obrigatório)\n" +
                      "- email: String (obrigatório, válido, único)\n" +
                      "- role: String (obrigatório, valores: ADMIN ou VENDEDOR)\n\n" +
                      "**Campos opcionais:**\n" +
                      "- password: String (opcional na atualização, se não informar mantém a senha atual)\n" +
                      "- avatar: MultipartFile (opcional, se não enviar mantém o avatar atual)"
    )
    public ResponseEntity<UsuarioResponse> update(
            @PathVariable UUID id,
            @Parameter(
                name = "usuario",
                description = "JSON com os dados do usuário (obrigatório). Exemplo:",
                required = true,
                examples = @ExampleObject(
                    name = "Exemplo de Usuário",
                    value = "{\"nome\":\"João Silva\",\"email\":\"joao@farmacia.com\",\"password\":\"senha123\",\"role\":\"ADMIN\"}",
                    summary = "Exemplo completo"
                )
            )
            @RequestPart("usuario") String usuarioJson,
            @Parameter(
                name = "avatar",
                description = "Arquivo de imagem do avatar (opcional). Se não enviar, mantém o avatar atual. Formatos: JPG, PNG, WebP. Tamanho máximo: 5MB."
            )
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) throws Exception {
        UsuarioRequest request = objectMapper.readValue(usuarioJson, UsuarioRequest.class);
        UsuarioResponse response = usuarioService.update(id, request, avatarFile);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar usuário", description = "Deleta um usuário. Retorna mensagem de confirmação. (apenas ADMIN)")
    public ResponseEntity<MensagemResponse> delete(@PathVariable UUID id) {
        String mensagem = usuarioService.delete(id);
        MensagemResponse response = new MensagemResponse(mensagem, id.toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload de avatar do usuário", 
        description = "Faz upload de avatar para um usuário (JPG/PNG/WebP, máx 5MB). ADMIN pode alterar qualquer avatar. VENDEDOR pode alterar apenas seu próprio avatar."
    )
    public ResponseEntity<UsuarioResponse> uploadAvatar(
            @Parameter(description = "ID do usuário") @PathVariable UUID id,
            @Parameter(description = "Arquivo de imagem do avatar (máx 5MB). Use o botão 'Choose File' para selecionar.", required = true)
            @RequestParam("file") MultipartFile file) {
        
        UsuarioResponse usuario = usuarioService.uploadAvatar(id, file);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }
}



