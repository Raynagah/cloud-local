package com.backend.carrito.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_ORDENES = "ordenes.exchange";
    public static final String QUEUE_CARRITO_LIMPIAR = "q.limpiar-carrito";
    public static final String ROUTING_KEY_ORDEN_CREADA = "orden.creada";

    @Bean
    public TopicExchange ordenesExchange() {
        return new TopicExchange(EXCHANGE_ORDENES, true, false);
    }

    @Bean
    public Queue limpiarCarritoQueue() {
        return QueueBuilder.durable(QUEUE_CARRITO_LIMPIAR).build();
    }

    @Bean
    public Binding bindingLimpiarCarrito(Queue limpiarCarritoQueue, TopicExchange ordenesExchange) {
        return BindingBuilder.bind(limpiarCarritoQueue)
                .to(ordenesExchange)
                .with(ROUTING_KEY_ORDEN_CREADA);
    }

    // Permite deserializar automáticamente el JSON recibido en un DTO Java
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
