package br.com.fiap.execucao.application.port.in;

import br.com.fiap.execucao.domain.valueobject.StatusFila;

import java.time.LocalDateTime;
import java.util.UUID;

public class FilaExecucaoDTO {

    public record Response(
            UUID id,
            UUID osId,
            StatusFila status,
            LocalDateTime dataEntrada,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {}
}
