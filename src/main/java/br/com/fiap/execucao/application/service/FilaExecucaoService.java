package br.com.fiap.execucao.application.service;

import br.com.fiap.execucao.application.port.in.FilaExecucaoDTO;
import br.com.fiap.execucao.application.port.in.FilaExecucaoUseCase;
import br.com.fiap.execucao.application.port.out.EventoPublicadorPort;
import br.com.fiap.execucao.application.port.out.FilaExecucaoRepositoryPort;
import br.com.fiap.execucao.domain.model.FilaExecucao;
import br.com.fiap.execucao.domain.valueobject.StatusFila;
import br.com.fiap.execucao.shared.exception.RecursoNaoEncontradoException;
import br.com.fiap.execucao.shared.exception.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilaExecucaoService implements FilaExecucaoUseCase {

    private final FilaExecucaoRepositoryPort filaRepository;
    private final EventoPublicadorPort eventoPublicador;

    @Override
    @Transactional
    public void entrarNaFila(UUID osId) {
        if (filaRepository.buscarPorOsId(osId).isPresent()) {
            log.warn("OS {} já está na fila de execução", osId);
            return;
        }
        FilaExecucao fila = FilaExecucao.builder()
                .osId(osId)
                .status(StatusFila.AGUARDANDO)
                .build();
        filaRepository.salvar(fila);
        log.info("OS {} entrou na fila de execução", osId);
    }

    @Override
    @Transactional
    public void cancelar(UUID osId) {
        filaRepository.buscarPorOsId(osId).ifPresent(fila -> {
            fila.setStatus(StatusFila.CANCELADA);
            filaRepository.salvar(fila);
            log.info("OS {} cancelada na fila de execução", osId);
        });
    }

    @Override
    @Transactional
    public FilaExecucaoDTO.Response iniciarExecucao(UUID osId) {
        FilaExecucao fila = buscarNaFila(osId);

        if (fila.getStatus() != StatusFila.AGUARDANDO) {
            throw new RegraDeNegocioException(
                    "OS " + osId + " não está com status AGUARDANDO. Status atual: " + fila.getStatus());
        }

        fila.setStatus(StatusFila.EM_EXECUCAO);
        fila.setDataInicio(LocalDateTime.now());
        return toResponse(filaRepository.salvar(fila));
    }

    @Override
    @Transactional
    public FilaExecucaoDTO.Response finalizarExecucao(UUID osId) {
        FilaExecucao fila = buscarNaFila(osId);

        if (fila.getStatus() != StatusFila.EM_EXECUCAO) {
            throw new RegraDeNegocioException(
                    "OS " + osId + " não está em execução. Status atual: " + fila.getStatus());
        }

        fila.setStatus(StatusFila.FINALIZADA);
        fila.setDataFim(LocalDateTime.now());
        FilaExecucao salva = filaRepository.salvar(fila);

        eventoPublicador.publicarExecucaoFinalizada(osId);
        log.info("OS {} finalizada e evento publicado", osId);

        return toResponse(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilaExecucaoDTO.Response> listarFila() {
        return filaRepository.listarOrdenadoPorEntrada().stream().map(this::toResponse).toList();
    }

    private FilaExecucao buscarNaFila(UUID osId) {
        return filaRepository.buscarPorOsId(osId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("OS não encontrada na fila: " + osId));
    }

    private FilaExecucaoDTO.Response toResponse(FilaExecucao fila) {
        return new FilaExecucaoDTO.Response(
                fila.getId(), fila.getOsId(), fila.getStatus(),
                fila.getDataEntrada(), fila.getDataInicio(), fila.getDataFim());
    }
}
