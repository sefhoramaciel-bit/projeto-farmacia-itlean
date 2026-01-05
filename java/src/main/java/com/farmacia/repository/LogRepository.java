package com.farmacia.repository;

import com.farmacia.domain.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LogRepository extends JpaRepository<Log, UUID> {
    List<Log> findTop100ByOrderByDataHoraDesc();
    List<Log> findTop50ByOrderByDataHoraDesc();
}


