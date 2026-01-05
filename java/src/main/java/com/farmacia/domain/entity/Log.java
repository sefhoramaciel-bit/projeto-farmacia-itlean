package com.farmacia.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tipoOperacao; // CREATE, UPDATE, DELETE, LOGIN

    @Column(nullable = false)
    private String tipoEntidade; // USUARIO, MEDICAMENTO, CATEGORIA, CLIENTE, VENDA, ESTOQUE, LOGIN

    @Column(nullable = false)
    private UUID entidadeId; // ID da entidade afetada

    @Column(nullable = false)
    private String descricao; // Descrição da operação

    @Column(columnDefinition = "TEXT")
    private String detalhes; // JSON ou detalhes da alteração

    @Column(nullable = false)
    private UUID usuarioId; // ID do usuário que realizou a operação

    @Column(nullable = false)
    private String usuarioNome; // Nome do usuário

    @Column(nullable = false)
    private String usuarioEmail; // Email do usuário

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @PrePersist
    protected void onCreate() {
        dataHora = LocalDateTime.now();
    }
}






