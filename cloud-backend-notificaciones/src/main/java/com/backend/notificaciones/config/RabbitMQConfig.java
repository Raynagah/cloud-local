package com.backend.notificaciones.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:pedidos.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue.notificaciones:q.enviar-notificacion}")
    private String queueNotificaciones;

    @Value("${app.rabbitmq.routingkey.orden-creada:pedido.creado}")
    private String routingKeyOrdenCreada;

    @Bean
    public Queue notificacionesQueue() {
        return new Queue(queueNotificaciones, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding bindingNotificaciones(Queue notificacionesQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notificacionesQueue).to(exchange).with(routingKeyOrdenCreada);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}