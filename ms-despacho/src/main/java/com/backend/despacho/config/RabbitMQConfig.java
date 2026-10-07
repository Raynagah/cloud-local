package com.backend.despacho.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

@Configuration
public class RabbitMQConfig {

    // 1. Constantes centralizadas (Punto 1)
    public static final String EXCHANGE_PEDIDOS = "pedidos.exchange";
    public static final String QUEUE_DESPACHO = "q.generar-despacho";
    public static final String ROUTING_KEY_DESPACHO = "pedido.creado";

    // 2. Beans de RabbitMQ (Punto 2)
    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(EXCHANGE_PEDIDOS);
    }

    @Bean
    public Queue despachoQueue() {
        return QueueBuilder.durable(QUEUE_DESPACHO).build();
    }

    @Bean
    public Binding bindingDespacho(Queue despachoQueue, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(despachoQueue)
                .to(pedidosExchange)
                .with(ROUTING_KEY_DESPACHO);
    }

    // Convertidor JSON con soporte para fechas Java 8
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}