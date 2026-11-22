package com.backend.pagos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.backend.pagos.model.EstadoPagoCliente;

@Repository
public interface EstadoPagoClienteRepositorio extends JpaRepository<EstadoPagoCliente, String> {
}