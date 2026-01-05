package com.farmacia.repository;

import com.farmacia.domain.entity.Venda;
import com.farmacia.domain.enums.StatusVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface VendaRepository extends JpaRepository<Venda, UUID> {
    List<Venda> findByStatus(StatusVenda status);
    List<Venda> findByClienteId(UUID clienteId);
    List<Venda> findByUsuarioId(UUID usuarioId);
    List<Venda> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fim);
}









