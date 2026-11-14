package br.com.fiap.execucao.adapter.out.persistence;

import br.com.fiap.execucao.application.port.out.FilaExecucaoRepositoryPort;
import br.com.fiap.execucao.domain.model.FilaExecucao;
import br.com.fiap.execucao.domain.valueobject.StatusFila;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FilaExecucaoRepositoryAdapter implements FilaExecucaoRepositoryPort {

    private final FilaExecucaoJpaRepository jpaRepository;

    @Override public FilaExecucao salvar(FilaExecucao fila) { return jpaRepository.save(fila); }
    @Override public Optional<FilaExecucao> buscarPorOsId(UUID osId) { return jpaRepository.findByOsId(osId); }
    @Override public List<FilaExecucao> listarPorStatus(StatusFila status) { return jpaRepository.findByStatusOrderByDataEntradaAsc(status); }
    @Override public List<FilaExecucao> listarOrdenadoPorEntrada() { return jpaRepository.findAllByOrderByDataEntradaAsc(); }
}
