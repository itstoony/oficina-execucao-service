package br.com.fiap.execucao.application.service;

import br.com.fiap.execucao.application.port.out.EventoPublicadorPort;
import br.com.fiap.execucao.application.port.out.FilaExecucaoRepositoryPort;
import br.com.fiap.execucao.domain.model.FilaExecucao;
import br.com.fiap.execucao.domain.valueobject.StatusFila;
import br.com.fiap.execucao.shared.exception.RecursoNaoEncontradoException;
import br.com.fiap.execucao.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilaExecucaoServiceTest {

    @Mock FilaExecucaoRepositoryPort filaRepository;
    @Mock EventoPublicadorPort eventoPublicador;
    @InjectMocks FilaExecucaoService filaExecucaoService;

    private UUID osId;
    private FilaExecucao filaAguardando;

    @BeforeEach
    void setUp() {
        osId = UUID.randomUUID();
        filaAguardando = FilaExecucao.builder()
                .id(UUID.randomUUID())
                .osId(osId)
                .status(StatusFila.AGUARDANDO)
                .dataEntrada(LocalDateTime.now())
                .build();
    }

    @Test
    void deveAdicionarOsNaFila() {
        when(filaRepository.buscarPorOsId(osId)).thenReturn(Optional.empty());
        when(filaRepository.salvar(any())).thenReturn(filaAguardando);

        filaExecucaoService.entrarNaFila(osId);

        verify(filaRepository).salvar(any());
    }

    @Test
    void deveIgnorarSeOsJaEstaNaFila() {
        when(filaRepository.buscarPorOsId(osId)).thenReturn(Optional.of(filaAguardando));

        filaExecucaoService.entrarNaFila(osId);

        verify(filaRepository, never()).salvar(any());
    }

    @Test
    void deveIniciarExecucaoComSucesso() {
        when(filaRepository.buscarPorOsId(osId)).thenReturn(Optional.of(filaAguardando));
        when(filaRepository.salvar(any())).thenReturn(filaAguardando);

        var response = filaExecucaoService.iniciarExecucao(osId);

        assertThat(response).isNotNull();
        verify(filaRepository).salvar(any());
    }

    @Test
    void deveRejeitarInicioQuandoStatusNaoEAguardando() {
        filaAguardando.setStatus(StatusFila.EM_EXECUCAO);
        when(filaRepository.buscarPorOsId(osId)).thenReturn(Optional.of(filaAguardando));

        assertThatThrownBy(() -> filaExecucaoService.iniciarExecucao(osId))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void deveFinalizarExecucaoEPublicarEvento() {
        filaAguardando.setStatus(StatusFila.EM_EXECUCAO);
        when(filaRepository.buscarPorOsId(osId)).thenReturn(Optional.of(filaAguardando));
        when(filaRepository.salvar(any())).thenReturn(filaAguardando);

        filaExecucaoService.finalizarExecucao(osId);

        verify(eventoPublicador).publicarExecucaoFinalizada(osId);
    }

    @Test
    void deveLancarExcecaoAoBuscarOsNaoExistente() {
        when(filaRepository.buscarPorOsId(osId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> filaExecucaoService.iniciarExecucao(osId))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveListarFilaOrdenada() {
        when(filaRepository.listarOrdenadoPorEntrada()).thenReturn(List.of(filaAguardando));

        var lista = filaExecucaoService.listarFila();

        assertThat(lista).hasSize(1);
    }
}
