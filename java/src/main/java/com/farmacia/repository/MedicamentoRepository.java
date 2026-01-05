package com.farmacia.repository;

import com.farmacia.domain.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, UUID> {
    Optional<Medicamento> findByNome(String nome);
    List<Medicamento> findByAtivoTrue();
    List<Medicamento> findByValidadeLessThanEqualAndAtivoTrue(LocalDate date);
    List<Medicamento> findByQuantidadeEstoqueLessThanAndAtivoTrue(Integer limite);
    boolean existsByCategoriaId(UUID categoriaId);
    long countByCategoriaId(UUID categoriaId);
}




