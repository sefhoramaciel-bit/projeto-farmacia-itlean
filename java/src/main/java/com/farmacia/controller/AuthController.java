package com.farmacia.controller;

import com.farmacia.domain.dto.LoginRequest;
import com.farmacia.domain.dto.LoginResponse;
import com.farmacia.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "Realizar login", 
        description = "Autentica o usuário e retorna o token JWT.\n\n" +
                      "**COMO USAR NO SWAGGER:**\n\n" +
                      "1. Clique em 'Try it out'\n" +
                      "2. No campo 'Request body', cole o JSON abaixo:\n" +
                      "```json\n" +
                      "{\"email\":\"admin@farmacia.com\",\"password\":\"admin123\"}\n" +
                      "```\n\n" +
                      "**FORMATO DO JSON:**\n" +
                      "```json\n" +
                      "{\n" +
                      "  \"email\": \"admin@farmacia.com\",\n" +
                      "  \"password\": \"admin123\"\n" +
                      "}\n" +
                      "```\n\n" +
                      "**Campos obrigatórios:**\n" +
                      "- email: String (obrigatório, formato de email válido)\n" +
                      "- password: String (obrigatório)\n\n" +
                      "**Resposta:**\n" +
                      "- Retorna um token JWT que deve ser usado no header 'Authorization: Bearer <token>' para acessar os outros endpoints."
    )
    public ResponseEntity<LoginResponse> login(
            @Parameter(
                description = "Credenciais de login",
                examples = @ExampleObject(
                    name = "Exemplo de Login",
                    value = "{\"email\":\"admin@farmacia.com\",\"password\":\"admin123\"}",
                    summary = "Exemplo completo"
                )
            )
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}








