package br.com.fiap.execucao.application.port.out;

import java.util.UUID;

public interface EventoPublicadorPort {
    void publicarEstoqueReservado(UUID osId);
    void publicarEstoqueIndisponivel(UUID osId, String motivo);
    void publicarExecucaoFinalizada(UUID osId);
}
