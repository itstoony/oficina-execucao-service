package br.com.fiap.execucao.adapter.out.persistence;

import br.com.fiap.execucao.application.port.out.MovimentacaoEstoqueRepositoryPort;
import br.com.fiap.execucao.domain.model.MovimentacaoEstoque;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MovimentacaoEstoqueRepositoryAdapter implements MovimentacaoEstoqueRepositoryPort {

    private final MovimentacaoEstoqueJpaRepository jpaRepository;

    @Override public MovimentacaoEstoque salvar(MovimentacaoEstoque m) { return jpaRepository.save(m); }
    @Override public List<MovimentacaoEstoque> buscarPorPecaIdOrdenado(UUID pecaId) {
        return jpaRepository.findByPecaIdOrderByDataMovimentacaoDesc(pecaId);
    }
}
