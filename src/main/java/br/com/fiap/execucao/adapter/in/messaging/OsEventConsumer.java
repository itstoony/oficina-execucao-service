package br.com.fiap.execucao.adapter.in.messaging;

import br.com.fiap.execucao.application.port.in.FilaExecucaoUseCase;
import br.com.fiap.execucao.application.port.out.EventoPublicadorPort;
import br.com.fiap.execucao.application.service.EstoqueService;
import br.com.fiap.execucao.shared.exception.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OsEventConsumer {

    private final EstoqueService estoqueService;
    private final FilaExecucaoUseCase filaExecucaoUseCase;
    private final EventoPublicadorPort eventoPublicador;

    @RabbitListener(queues = "#{@rabbitMQConfig.execucaoQueue()}")
    public void consumir(Map<String, Object> payload, org.springframework.amqp.core.Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info("Evento recebido: {} | payload: {}", routingKey, payload);

        switch (routingKey) {
            case "os.aprovada" -> processarOsAprovada(payload);
            case "os.cancelada" -> processarOsCancelada(payload);
            case "pagamento.confirmado" -> processarPagamentoConfirmado(payload);
            default -> log.warn("Routing key desconhecida: {}", routingKey);
        }
    }

    private void processarOsAprovada(Map<String, Object> payload) {
        UUID osId = UUID.fromString(payload.get("osId").toString());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itensPeca = (List<Map<String, Object>>) payload.get("itensPeca");

        if (itensPeca == null || itensPeca.isEmpty()) {
            eventoPublicador.publicarEstoqueReservado(osId);
            return;
        }

        try {
            for (Map<String, Object> item : itensPeca) {
                UUID pecaId = UUID.fromString(item.get("pecaId").toString());
                Integer quantidade = Integer.parseInt(item.get("quantidade").toString());
                estoqueService.verificarDisponibilidadeEReservar(pecaId, quantidade, osId);
            }
            eventoPublicador.publicarEstoqueReservado(osId);
        } catch (RegraDeNegocioException ex) {
            log.error("Estoque insuficiente para OS {}: {}", osId, ex.getMessage());
            eventoPublicador.publicarEstoqueIndisponivel(osId, ex.getMessage());
        }
    }

    private void processarOsCancelada(Map<String, Object> payload) {
        UUID osId = UUID.fromString(payload.get("osId").toString());
        filaExecucaoUseCase.cancelar(osId);
    }

    private void processarPagamentoConfirmado(Map<String, Object> payload) {
        UUID osId = UUID.fromString(payload.get("osId").toString());
        filaExecucaoUseCase.entrarNaFila(osId);
    }
}
