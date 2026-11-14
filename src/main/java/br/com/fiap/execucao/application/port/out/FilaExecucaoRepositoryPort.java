package br.com.fiap.execucao.application.port.out;

import br.com.fiap.execucao.domain.model.FilaExecucao;
import br.com.fiap.execucao.domain.valueobject.StatusFila;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FilaExecucaoRepositoryPort {
    FilaExecucao salvar(FilaExecucao fila);
    Optional<FilaExecucao> buscarPorOsId(UUID osId);
    List<FilaExecucao> listarPorStatus(StatusFila status);
    List<FilaExecucao> listarOrdenadoPorEntrada();
}
