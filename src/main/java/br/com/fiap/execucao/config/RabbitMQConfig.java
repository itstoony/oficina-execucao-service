package br.com.fiap.execucao.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "oficina.events";
    private static final String QUEUE = "execucao.os.queue";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue execucaoQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding bindingOsAprovada(Queue execucaoQueue, TopicExchange exchange) {
        return BindingBuilder.bind(execucaoQueue).to(exchange).with("os.aprovada");
    }

    @Bean
    public Binding bindingOsCancelada(Queue execucaoQueue, TopicExchange exchange) {
        return BindingBuilder.bind(execucaoQueue).to(exchange).with("os.cancelada");
    }

    @Bean
    public Binding bindingPagamentoConfirmado(Queue execucaoQueue, TopicExchange exchange) {
        return BindingBuilder.bind(execucaoQueue).to(exchange).with("pagamento.confirmado");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
