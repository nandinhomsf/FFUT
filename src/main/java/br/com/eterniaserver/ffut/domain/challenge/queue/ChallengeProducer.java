package br.com.eterniaserver.ffut.domain.challenge.queue;

import lombok.AllArgsConstructor;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ChallengeProducer {

    private final AmqpTemplate amqpTemplate;

    public void sendChallengeToWorker(String challengeAnswerId) {
        amqpTemplate.convertAndSend("challenge_exchange", "challenge_routing", challengeAnswerId);
    }
}
