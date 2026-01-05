package com.farmacia.repository;

import com.farmacia.domain.entity.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, UUID> {
    List<Alerta> findByLidoFalse();
    List<Alerta> findByMedicamentoId(UUID medicamentoId);
    List<Alerta> findByTipoAndLidoFalse(String tipo);
}




