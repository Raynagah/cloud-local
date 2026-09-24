package com.backend.ordenes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.ordenes.model.Orden;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
}