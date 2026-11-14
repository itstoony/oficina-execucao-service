package br.com.fiap.execucao.application.service;

import br.com.fiap.execucao.application.port.out.MovimentacaoEstoqueRepositoryPort;
import br.com.fiap.execucao.application.port.out.PecaRepositoryPort;
import br.com.fiap.execucao.domain.model.Peca;
import br.com.fiap.execucao.shared.exception.RecursoNaoEncontradoException;
import br.com.fiap.execucao.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock private PecaRepositoryPort pecaRepository;
    @Mock private MovimentacaoEstoqueRepositoryPort movimentacaoRepository;

    @InjectMocks private EstoqueService service;

    private Peca peca;

    @BeforeEach
    void setUp() {
        peca = Peca.builder()
                .id(UUID.randomUUID()).nome("Filtro de óleo")
                .qtdEstoque(10).qtdReservada(2).qtdMinima(3)
                .precoUnitario(new BigDecimal("50.00")).build();
    }

    @Test
    @DisplayName("Deve reservar estoque quando disponível")
    void deveReservarEstoqueQuandoDisponivel() {
        when(pecaRepository.buscarPorId(peca.getId())).thenReturn(Optional.of(peca));
        when(pecaRepository.salvar(any())).thenReturn(peca);
        when(movimentacaoRepository.salvar(any())).thenReturn(null);

        service.verificarDisponibilidadeEReservar(peca.getId(), 3, UUID.randomUUID());

        verify(pecaRepository).salvar(peca);
        verify(movimentacaoRepository).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque insuficiente")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        when(pecaRepository.buscarPorId(peca.getId())).thenReturn(Optional.of(peca));

        assertThatThrownBy(() -> service.verificarDisponibilidadeEReservar(peca.getId(), 100, UUID.randomUUID()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    @DisplayName("Deve lançar exceção quando peça não encontrada")
    void deveLancarExcecaoQuandoPecaNaoEncontrada() {
        UUID pecaId = UUID.randomUUID();
        when(pecaRepository.buscarPorId(pecaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verificarDisponibilidadeEReservar(pecaId, 1, UUID.randomUUID()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve liberar reserva quando peça existe")
    void deveLiberarReserva() {
        when(pecaRepository.buscarPorId(peca.getId())).thenReturn(Optional.of(peca));
        when(pecaRepository.salvar(any())).thenReturn(peca);
        when(movimentacaoRepository.salvar(any())).thenReturn(null);

        service.liberarReserva(peca.getId(), 2, UUID.randomUUID());

        verify(pecaRepository).salvar(peca);
    }

    @Test
    @DisplayName("Deve ignorar liberação quando peça não existe")
    void deveIgnorarLiberacaoQuandoPecaNaoExiste() {
        when(pecaRepository.buscarPorId(any())).thenReturn(Optional.empty());

        service.liberarReserva(UUID.randomUUID(), 2, UUID.randomUUID());

        verify(pecaRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve baixar estoque")
    void deveBaixarEstoque() {
        when(pecaRepository.buscarPorId(peca.getId())).thenReturn(Optional.of(peca));
        when(pecaRepository.salvar(any())).thenReturn(peca);
        when(movimentacaoRepository.salvar(any())).thenReturn(null);

        service.baixarEstoque(UUID.randomUUID(), peca.getId(), 2);

        verify(pecaRepository).salvar(peca);
        verify(movimentacaoRepository).salvar(any());
    }
}
