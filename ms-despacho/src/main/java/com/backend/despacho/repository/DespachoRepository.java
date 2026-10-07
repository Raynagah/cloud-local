package com.backend.despacho.repository;

import com.backend.despacho.model.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DespachoRepository extends JpaRepository<Despacho, Long> {
    Optional<Despacho> findByOrdenId(Long ordenId);
    
    // Buscar todos los despachos de un usuario específico usando su ID de Azure
    List<Despacho> findAllByUsuarioIdOrderByFechaActualizacionDesc(String usuarioId);
}