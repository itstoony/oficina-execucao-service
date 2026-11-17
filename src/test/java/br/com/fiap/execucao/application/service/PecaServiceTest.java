package br.com.fiap.execucao.application.service;

import br.com.fiap.execucao.application.port.in.PecaDTO;
import br.com.fiap.execucao.application.port.out.MovimentacaoEstoqueRepositoryPort;
import br.com.fiap.execucao.application.port.out.PecaRepositoryPort;
import br.com.fiap.execucao.domain.model.Peca;
import br.com.fiap.execucao.shared.exception.RecursoNaoEncontradoException;
import br.com.fiap.execucao.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PecaServiceTest {

    @Mock PecaRepositoryPort pecaRepository;
    @Mock MovimentacaoEstoqueRepositoryPort movimentacaoRepository;
    @InjectMocks PecaService pecaService;

    private Peca pecaPadrao;

    @BeforeEach
    void setUp() {
        pecaPadrao = Peca.builder()
                .id(UUID.randomUUID())
                .nome("Filtro de óleo")
                .codigo("FO-001")
                .precoUnitario(new BigDecimal("25.00"))
                .qtdEstoque(10)
                .qtdReservada(0)
                .qtdMinima(2)
                .build();
    }

    @Test
    void deveCadastrarPecaComSucesso() {
        var request = new PecaDTO.CadastrarRequest("Filtro de óleo", "FO-001", new BigDecimal("25.00"), 10, 2);
        when(pecaRepository.existePorCodigo("FO-001")).thenReturn(false);
        when(pecaRepository.salvar(any())).thenReturn(pecaPadrao);
        when(movimentacaoRepository.salvar(any())).thenReturn(null);

        var response = pecaService.cadastrar(request);

        assertThat(response.nome()).isEqualTo("Filtro de óleo");
        assertThat(response.qtdEstoque()).isEqualTo(10);
        verify(pecaRepository).salvar(any());
        verify(movimentacaoRepository).salvar(any());
    }

    @Test
    void deveRejeitarCadastroPecaComCodigoDuplicado() {
        var request = new PecaDTO.CadastrarRequest("Filtro", "FO-001", new BigDecimal("25.00"), 5, 1);
        when(pecaRepository.existePorCodigo("FO-001")).thenReturn(true);

        assertThatThrownBy(() -> pecaService.cadastrar(request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("FO-001");
    }

    @Test
    void deveBuscarPecaPorIdComSucesso() {
        when(pecaRepository.buscarPorId(pecaPadrao.getId())).thenReturn(Optional.of(pecaPadrao));

        var response = pecaService.buscarPorId(pecaPadrao.getId());

        assertThat(response.id()).isEqualTo(pecaPadrao.getId());
    }

    @Test
    void deveLancarExcecaoQuandoPecaNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(pecaRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pecaService.buscarPorId(id))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveListarTodasAsPecas() {
        when(pecaRepository.listarTodos()).thenReturn(List.of(pecaPadrao));

        var lista = pecaService.listar();

        assertThat(lista).hasSize(1);
    }

    @Test
    void deveAtualizarPecaComSucesso() {
        var request = new PecaDTO.AtualizarRequest("Filtro atualizado", new BigDecimal("30.00"), 3, null);
        when(pecaRepository.buscarPorId(pecaPadrao.getId())).thenReturn(Optional.of(pecaPadrao));
        when(pecaRepository.salvar(any())).thenReturn(pecaPadrao);

        var response = pecaService.atualizar(pecaPadrao.getId(), request);

        assertThat(response).isNotNull();
        verify(pecaRepository).salvar(any());
    }

    @Test
    void deveBloquearExclusaoQuandoHaReserva() {
        pecaPadrao.setQtdReservada(2);
        when(pecaRepository.buscarPorId(pecaPadrao.getId())).thenReturn(Optional.of(pecaPadrao));

        assertThatThrownBy(() -> pecaService.excluir(pecaPadrao.getId()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("reservada");
    }

    @Test
    void deveRegistrarEntradaDeEstoque() {
        var request = new PecaDTO.EntradaRequest(5, "Reposição");
        when(pecaRepository.buscarPorId(pecaPadrao.getId())).thenReturn(Optional.of(pecaPadrao));
        when(pecaRepository.salvar(any())).thenReturn(pecaPadrao);
        when(movimentacaoRepository.salvar(any())).thenReturn(null);

        var response = pecaService.registrarEntrada(pecaPadrao.getId(), request);

        assertThat(response).isNotNull();
        verify(movimentacaoRepository).salvar(any());
    }

    @Test
    void deveIdentificarEstoqueCritico() {
        pecaPadrao.setQtdEstoque(1);
        when(pecaRepository.buscarCriticas()).thenReturn(List.of(pecaPadrao));

        var criticas = pecaService.buscarCriticas();

        assertThat(criticas).hasSize(1);
        assertThat(criticas.get(0).estoqueCritico()).isTrue();
    }

    @Test
    void deveCalcularQtdDisponivelCorretamente() {
        pecaPadrao.setQtdEstoque(10);
        pecaPadrao.setQtdReservada(3);

        assertThat(pecaPadrao.getQtdDisponivel()).isEqualTo(7);
    }
}
