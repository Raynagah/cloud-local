package com.backend.pagos.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_PEDIDOS = "pedidos.exchange";
    public static final String ROUTING_KEY_PAGO_EXITOSO = "pago.exitoso";

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(EXCHANGE_PEDIDOS);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}