package com.backend.ordenes.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_PEDIDOS = "pedidos.exchange";

    // Routing Keys
    public static final String ROUTING_KEY_PEDIDO_CREADO = "pedido.creado";

    // Colas
    public static final String QUEUE_STOCK = "q.actualizar-stock";
    public static final String QUEUE_NOTIFICACION = "q.enviar-notificacion";
    public static final String QUEUE_DESPACHO = "q.generar-despacho";
    public static final String QUEUE_CARRITO_LIMPIAR = "q.limpiar-carrito";

    @Bean

    public TopicExchange pedidosExchange() {
        return new TopicExchange(EXCHANGE_PEDIDOS);
    }

    @Bean
    public Queue stockQueue() {
        return new Queue(QUEUE_STOCK, true);
    }

    @Bean
    public Queue notificacionQueue() {
        return new Queue(QUEUE_NOTIFICACION, true);
    }

    @Bean
    public Queue despachoQueue() {
        return new Queue(QUEUE_DESPACHO, true);
    }

    // Enlaces (Bindings) de las colas al Exchange
    @Bean
    public Binding stockBinding(Queue stockQueue, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(stockQueue).to(pedidosExchange).with(ROUTING_KEY_PEDIDO_CREADO);
    }

    @Bean
    public Binding notificacionBinding(Queue notificacionQueue, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(notificacionQueue).to(pedidosExchange).with(ROUTING_KEY_PEDIDO_CREADO);
    }

    @Bean
    public Binding despachoBinding(Queue despachoQueue, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(despachoQueue).to(pedidosExchange).with(ROUTING_KEY_PEDIDO_CREADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue carritoLimpiarQueue() {
        return new Queue(QUEUE_CARRITO_LIMPIAR, true);
    }

    @Bean
    public Binding carritoLimpiarBinding(Queue carritoLimpiarQueue, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(carritoLimpiarQueue).to(pedidosExchange).with(ROUTING_KEY_PEDIDO_CREADO);
    }
}
