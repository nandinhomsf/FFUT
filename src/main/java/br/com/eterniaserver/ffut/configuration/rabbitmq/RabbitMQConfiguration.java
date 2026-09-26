package br.com.eterniaserver.ffut.configuration.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    @Bean
    public Queue challengerQueue() {
        return new Queue("challenge");
    }

    @Bean
    public TopicExchange challengerExchange() {
        return new TopicExchange("challenge_exchange");
    }

    @Bean
    public Binding bindingChallenge(Queue challengerQueue, TopicExchange challengerExchange) {
        return BindingBuilder
                .bind(challengerQueue)
                .to(challengerExchange)
                .with("challenge_routing");
    }
}
