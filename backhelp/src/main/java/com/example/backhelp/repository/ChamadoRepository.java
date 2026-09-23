package com.example.backhelp.repository;

import com.example.backhelp.model.ChamadoModel;
import com.example.backhelp.model.Perfil;
import com.example.backhelp.model.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChamadoRepository extends JpaRepository<ChamadoModel, Long> {


    Optional<ChamadoModel> findByProtocolo(String protocolo);


    List<ChamadoModel> findByUsuarioAberturaId(Long usuarioId);


    List<ChamadoModel> findByAtendenteResponsavelId(Long atendenteId);


    List<ChamadoModel> findByStatus(StatusChamado status);


    List<ChamadoModel> findByNivelAtendimento(Perfil nivelAtendimento);


    long countByStatus(StatusChamado status);


    long countByDataCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);
}