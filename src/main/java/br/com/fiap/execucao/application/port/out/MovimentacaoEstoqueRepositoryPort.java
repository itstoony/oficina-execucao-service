package br.com.fiap.execucao.application.port.out;

import br.com.fiap.execucao.domain.model.MovimentacaoEstoque;

import java.util.List;
import java.util.UUID;

public interface MovimentacaoEstoqueRepositoryPort {
    MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao);
    List<MovimentacaoEstoque> buscarPorPecaIdOrdenado(UUID pecaId);
}
