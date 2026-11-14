package br.com.fiap.execucao.adapter.out.persistence;

import br.com.fiap.execucao.domain.model.FilaExecucao;
import br.com.fiap.execucao.domain.valueobject.StatusFila;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FilaExecucaoJpaRepository extends JpaRepository<FilaExecucao, UUID> {
    Optional<FilaExecucao> findByOsId(UUID osId);
    List<FilaExecucao> findByStatusOrderByDataEntradaAsc(StatusFila status);
    List<FilaExecucao> findAllByOrderByDataEntradaAsc();
}
