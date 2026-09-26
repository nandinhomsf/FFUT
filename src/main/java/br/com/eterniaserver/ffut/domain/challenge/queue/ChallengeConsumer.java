package br.com.eterniaserver.ffut.domain.challenge.queue;

import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ProcessRunnerEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ResultCondenserEntity;
import br.com.eterniaserver.ffut.domain.challenge.enums.AnswerStatus;
import br.com.eterniaserver.ffut.domain.challenge.repositories.ChallengeAnswerRepository;
import br.com.eterniaserver.ffut.domain.challenge.repositories.ChallengeRepository;
import br.com.eterniaserver.ffut.domain.challenge.services.TestRunnerService;

import br.com.eterniaserver.ffut.domain.user.entities.UserAccountEntity;
import br.com.eterniaserver.ffut.domain.user.repositories.UserAccountRepository;
import lombok.Data;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.logging.Logger;

@Data
@Service
public class ChallengeConsumer {

    private static final Logger LOGGER = Logger.getLogger(ChallengeConsumer.class.getName());

    private final ChallengeAnswerRepository challengeAnswerRepository;
    private final ChallengeRepository challengeRepository;
    private final UserAccountRepository userAccountRepository;

    private final TestRunnerService testRunnerService;

    @RabbitListener(queues = "challenge")
    public void receiveChallenger(String challengeAnswerId) {
        Optional<ChallengeAnswerEntity> challengeAnswerOptional = challengeAnswerRepository.findById(challengeAnswerId);

        if (challengeAnswerOptional.isEmpty()) {
            LOGGER.warning("Challenge answer not found: " + challengeAnswerId);
            return;
        }

        ChallengeAnswerEntity answer = challengeAnswerOptional.get();

        answer.setStatus(AnswerStatus.PROCESSING);

        challengeAnswerRepository.save(answer);

        condenseAndProcess(answer);

        Optional<ChallengeEntity> challengeOptional = challengeRepository.findById(answer.getChallengeId());
        Optional<UserAccountEntity> userOptional = userAccountRepository.findById(answer.getUserId());

        if (challengeOptional.isEmpty()) {
            LOGGER.warning("Challenge not found: " + answer.getChallengeId());
            return;
        } else if (userOptional.isEmpty()) {
            LOGGER.warning("User not found: " + answer.getUserId());
            return;
        }

        ChallengeEntity challenge = challengeOptional.get();
        UserAccountEntity user = userOptional.get();

        challenge.addToRank(answer);
        user.updateScore(challenge, answer);

        challengeRepository.save(challenge);
        userAccountRepository.save(user);
    }

    private void condenseAndProcess(ChallengeAnswerEntity challengeAnswer) {
        ProcessRunnerEntity processRunner = testRunnerService.run(challengeAnswer);

        ResultCondenserEntity resultCondenser = testRunnerService.condense(processRunner);

        resultCondenser.condenseResults();
        resultCondenser.generateScore();

        if (resultCondenser.getResultModel().getScore() > 70) {
            challengeAnswer.setStatus(AnswerStatus.CORRECT);
        } else {
            challengeAnswer.setStatus(AnswerStatus.INCORRECT);
        }

        challengeAnswer.setChallengeResult(resultCondenser.getResultModel());

        challengeAnswerRepository.save(challengeAnswer);
    }
}
