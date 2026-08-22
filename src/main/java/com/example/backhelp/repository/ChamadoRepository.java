package com.example.backhelp.repository;

import com.example.backhelp.model.ChamadoModel;
import com.example.backhelp.model.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<ChamadoModel, Long> {

    List<ChamadoModel> findBySolicitanteId(Long solicitanteId);

    long countBySolicitanteIdAndStatusNotIn(Long solicitanteId, List<StatusChamado> statusFinalizados);
}


