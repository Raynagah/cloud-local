package com.backend.ordenes.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.backend.ordenes.model.Orden;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findAllByUsuarioCorreoOrderByFechaCreacionDesc(String usuarioCorreo);
}