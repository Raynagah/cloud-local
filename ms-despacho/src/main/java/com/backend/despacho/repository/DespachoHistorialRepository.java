package com.backend.despacho.repository;

import com.backend.despacho.model.DespachoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DespachoHistorialRepository extends JpaRepository<DespachoHistorial, Long> {
}