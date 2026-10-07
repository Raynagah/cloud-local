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

    public static final String EXCHANGE_USUARIOS = "usuarios.exchange";
    public static final String QUEUE_USUARIO_REGISTRADO = "q.notificaciones.usuario-registrado";
    public static final String QUEUE_USUARIO_ACTUALIZADO = "q.notificaciones.usuario-actualizado";
    
    public static final String ROUTING_KEY_USUARIO_REGISTRADO = "usuario.registrado";
    public static final String ROUTING_KEY_USUARIO_ACTUALIZADO = "usuario.actualizado";

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


    // --- BEANS USUARIOS ---
    @Bean
    public TopicExchange usuariosExchange() {
        return new TopicExchange(EXCHANGE_USUARIOS, true, false);
    }

    @Bean
    public Queue usuarioRegistradoQueue() {
        return QueueBuilder.durable(QUEUE_USUARIO_REGISTRADO).build();
    }

    @Bean
    public Queue usuarioActualizadoQueue() {
        return QueueBuilder.durable(QUEUE_USUARIO_ACTUALIZADO).build();
    }

    @Bean
    public Binding bindingUsuarioRegistrado(Queue usuarioRegistradoQueue, TopicExchange usuariosExchange) {
        return BindingBuilder.bind(usuarioRegistradoQueue)
                .to(usuariosExchange)
                .with(ROUTING_KEY_USUARIO_REGISTRADO);
    }

    @Bean
    public Binding bindingUsuarioActualizado(Queue usuarioActualizadoQueue, TopicExchange usuariosExchange) {
        return BindingBuilder.bind(usuarioActualizadoQueue)
                .to(usuariosExchange)
                .with(ROUTING_KEY_USUARIO_ACTUALIZADO);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}