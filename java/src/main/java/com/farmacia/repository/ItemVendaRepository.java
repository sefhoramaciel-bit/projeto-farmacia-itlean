package com.farmacia.repository;

import com.farmacia.domain.entity.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, UUID> {
    boolean existsByMedicamentoId(UUID medicamentoId);
    long countByMedicamentoId(UUID medicamentoId);
}

