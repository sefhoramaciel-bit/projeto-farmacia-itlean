package com.farmacia.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        // Pega apenas o primeiro erro para retornar uma mensagem específica
        if (!ex.getBindingResult().getAllErrors().isEmpty()) {
            FieldError firstError = (FieldError) ex.getBindingResult().getAllErrors().get(0);
            String errorMessage = firstError.getDefaultMessage();
            // Retorna no formato esperado pelo frontend
            errors.put("error", errorMessage);
        } else {
            errors.put("error", "Erro de validação nos dados fornecidos, por favor verifique os campos.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Credenciais inválidas");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, String>> handleTransactionException(TransactionSystemException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Erro na transação");
        Throwable rootCause = ex.getRootCause();
        if (rootCause != null) {
            error.put("message", rootCause.getMessage());
        } else {
            error.put("message", ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, String> error = new HashMap<>();
        String message = ex.getMessage();
        
        // Detecta violações de campos únicos e retorna mensagens mais específicas
        if (message != null) {
            if (message.contains("medicamentos_nome") || message.contains("medicamento") && message.contains("nome")) {
                error.put("error", "O nome do medicamento já existe, por favor alterar.");
            } else if (message.contains("categorias_nome") || message.contains("categoria") && message.contains("nome")) {
                error.put("error", "O nome da categoria já existe, por favor alterar.");
            } else if (message.contains("clientes_cpf") || message.contains("cpf")) {
                error.put("error", "O CPF informado já existe, por favor alterar.");
            } else if (message.contains("clientes_email") || message.contains("usuarios_email") || 
                      (message.contains("email") && (message.contains("cliente") || message.contains("usuario")))) {
                error.put("error", "O email informado já existe, por favor alterar.");
            } else {
                // Mensagem genérica para outras violações
                error.put("error", "Violação de integridade de dados: " + (ex.getRootCause() != null ? ex.getRootCause().getMessage() : message));
            }
        } else {
            error.put("error", "Violação de integridade de dados.");
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Erro interno do servidor");
        error.put("message", ex.getMessage());
        // Log da stack trace completa para debug
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}




