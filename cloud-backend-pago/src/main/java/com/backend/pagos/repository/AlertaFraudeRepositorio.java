package com.backend.pagos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.pagos.model.AlertaFraude;

@Repository
public interface AlertaFraudeRepositorio extends JpaRepository<AlertaFraude, Long> {

    List<AlertaFraude> findTop50ByOrderByDetectadoEnDesc();
}
