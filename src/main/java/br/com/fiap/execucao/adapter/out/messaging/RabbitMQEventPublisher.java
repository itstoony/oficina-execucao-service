package br.com.fiap.execucao.adapter.out.messaging;

import br.com.fiap.execucao.application.port.out.EventoPublicadorPort;
import br.com.fiap.execucao.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQEventPublisher implements EventoPublicadorPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publicarEstoqueReservado(UUID osId) {
        Map<String, String> payload = Map.of("osId", osId.toString());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "estoque.reservado", payload);
        log.info("Evento estoque.reservado publicado para OS {}", osId);
    }

    @Override
    public void publicarEstoqueIndisponivel(UUID osId, String motivo) {
        Map<String, String> payload = Map.of("osId", osId.toString(), "motivo", motivo);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "estoque.indisponivel", payload);
        log.warn("Evento estoque.indisponivel publicado para OS {}: {}", osId, motivo);
    }

    @Override
    public void publicarExecucaoFinalizada(UUID osId) {
        Map<String, String> payload = Map.of("osId", osId.toString());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "execucao.finalizada", payload);
        log.info("Evento execucao.finalizada publicado para OS {}", osId);
    }
}
