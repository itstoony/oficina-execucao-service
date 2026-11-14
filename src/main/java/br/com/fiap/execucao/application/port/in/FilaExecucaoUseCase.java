package br.com.fiap.execucao.application.port.in;

import java.util.List;
import java.util.UUID;

public interface FilaExecucaoUseCase {
    void entrarNaFila(UUID osId);
    void cancelar(UUID osId);
    FilaExecucaoDTO.Response iniciarExecucao(UUID osId);
    FilaExecucaoDTO.Response finalizarExecucao(UUID osId);
    List<FilaExecucaoDTO.Response> listarFila();
}
