package br.com.eterniaserver.ffut.domain.challenge.queue;

import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ProcessRunnerEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ResultCondenserEntity;
import br.com.eterniaserver.ffut.domain.challenge.repositories.ChallengeAnswerRepository;
import br.com.eterniaserver.ffut.domain.challenge.repositories.ChallengeRepository;
import br.com.eterniaserver.ffut.domain.challenge.services.TestRunnerService;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.ChallengeResultEntity;

import br.com.eterniaserver.ffut.domain.user.entities.UserAccountEntity;
import br.com.eterniaserver.ffut.domain.user.repositories.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import java.util.Optional;

class ChallengeConsumerTest {

    private ChallengeAnswerRepository challengeAnswerRepositoryMock;
    private ChallengeRepository challengeRepositoryMock;
    private UserAccountRepository userAccountRepositoryMock;
    private TestRunnerService testRunnerServiceMock;

    private ChallengeConsumer challengeConsumer;

    @BeforeEach
    void setUp() {
        challengeAnswerRepositoryMock = Mockito.mock(ChallengeAnswerRepository.class);
        challengeRepositoryMock = Mockito.mock(ChallengeRepository.class);
        userAccountRepositoryMock = Mockito.mock(UserAccountRepository.class);
        testRunnerServiceMock = Mockito.mock(TestRunnerService.class);

        challengeConsumer = new ChallengeConsumer(
                challengeAnswerRepositoryMock,
                challengeRepositoryMock,
                userAccountRepositoryMock,
                testRunnerServiceMock
        );
    }

    @Test
    void shouldNotProcessChallengeAnswerIfItDoesNotExist() {
        // Arrange

        String challengeAnswerId = "$941DAD1RHA124A5VA5124";

        Mockito.when(challengeAnswerRepositoryMock.findById(challengeAnswerId))
                .thenReturn(Optional.empty());

        // Act

        challengeConsumer.receiveChallenger(challengeAnswerId);

        // Assert

        Mockito.verify(challengeAnswerRepositoryMock, Mockito.times(0))
                .save(Mockito.any(ChallengeAnswerEntity.class));
    }

    @Test
    void shouldRunAndCondenseResultsFromChallengeAnswer() {
        // Arrange

        String challengeAnswerId = "$941DAD1RHA124A5VA5124";
        String challengeId = "HA341GF1HA24A5VA5124";

        ChallengeAnswerEntity challengeAnswer = Mockito.mock(ChallengeAnswerEntity.class);
        ProcessRunnerEntity processRunner = Mockito.mock(ProcessRunnerEntity.class);
        ResultCondenserEntity resultCondenser = Mockito.mock(ResultCondenserEntity.class);
        ChallengeResultEntity challengeResult = Mockito.mock(ChallengeResultEntity.class);

        Mockito.when(challengeAnswer.getId())
                .thenReturn(challengeAnswerId);
        Mockito.when(challengeAnswer.getChallengeId())
                .thenReturn(challengeId);
        Mockito.when(resultCondenser.getResultModel())
                .thenReturn(challengeResult);
        Mockito.when(challengeResult.getScore())
                .thenReturn(0D);

        Mockito.when(challengeAnswerRepositoryMock.findById(challengeAnswerId))
                .thenReturn(Optional.of(challengeAnswer));
        Mockito.when(testRunnerServiceMock.run(challengeAnswer))
                .thenReturn(processRunner);
        Mockito.when(testRunnerServiceMock.condense(processRunner))
                .thenReturn(resultCondenser);

        // Act

        challengeConsumer.receiveChallenger(challengeAnswerId);

        // Assert

        Mockito.verify(resultCondenser, Mockito.times(1))
                .condenseResults();
        Mockito.verify(resultCondenser, Mockito.times(1))
                .generateScore();

        Mockito.verify(challengeAnswerRepositoryMock, Mockito.times(2))
                .save(challengeAnswer);
    }

    @Test
    void shouldNotAddToRankWhenChallengeNotFound() {
        // Arrange

        String challengeAnswerId = "$941DAD1RHA124A5VA5124";
        String challengeId = "HA341GF1HA24A5VA5124";

        ChallengeAnswerEntity challengeAnswer = Mockito.mock(ChallengeAnswerEntity.class);
        ProcessRunnerEntity processRunner = Mockito.mock(ProcessRunnerEntity.class);
        ResultCondenserEntity resultCondenser = Mockito.mock(ResultCondenserEntity.class);
        ChallengeResultEntity challengeResult = Mockito.mock(ChallengeResultEntity.class);

        Mockito.when(challengeAnswer.getId())
                .thenReturn(challengeAnswerId);
        Mockito.when(challengeAnswer.getChallengeId())
                .thenReturn(challengeId);
        Mockito.when(resultCondenser.getResultModel())
                .thenReturn(challengeResult);
        Mockito.when(challengeResult.getScore())
                .thenReturn(0D);

        Mockito.when(challengeAnswerRepositoryMock.findById(challengeAnswerId))
                .thenReturn(Optional.of(challengeAnswer));
        Mockito.when(testRunnerServiceMock.run(challengeAnswer))
                .thenReturn(processRunner);
        Mockito.when(testRunnerServiceMock.condense(processRunner))
                .thenReturn(resultCondenser);
        Mockito.when(challengeRepositoryMock.findById(challengeAnswerId))
                .thenReturn(Optional.empty());

        // Act

        challengeConsumer.receiveChallenger(challengeAnswerId);

        // Assert

        Mockito.verify(challengeRepositoryMock, Mockito.times(0))
                .save(Mockito.any());
    }

    @Test
    void shouldAddToRankAfterProcessAndCondenseChallengeAnswer() {
        // Arrange

        String challengeAnswerId = "$941DAD1RHA124A5VA5124";
        String challengeId = "HA341GF1HA24A5VA5124";
        String userId = "2VSD0F1HA24A5VA5124";

        ChallengeAnswerEntity challengeAnswer = Mockito.mock(ChallengeAnswerEntity.class);
        ProcessRunnerEntity processRunner = Mockito.mock(ProcessRunnerEntity.class);
        ResultCondenserEntity resultCondenser = Mockito.mock(ResultCondenserEntity.class);
        ChallengeEntity challenge = Mockito.mock(ChallengeEntity.class);
        ChallengeResultEntity challengeResult = Mockito.mock(ChallengeResultEntity.class);
        UserAccountEntity user = Mockito.mock(UserAccountEntity.class);

        Mockito.when(challengeAnswer.getId())
                .thenReturn(challengeAnswerId);
        Mockito.when(challengeAnswer.getChallengeId())
                .thenReturn(challengeId);
        Mockito.when(resultCondenser.getResultModel())
                .thenReturn(challengeResult);
        Mockito.when(challengeResult.getScore())
                .thenReturn(0D);
        Mockito.when(challengeAnswer.getUserId())
                .thenReturn(userId);

        Mockito.when(challengeAnswerRepositoryMock.findById(challengeAnswerId))
                .thenReturn(Optional.of(challengeAnswer));
        Mockito.when(testRunnerServiceMock.run(challengeAnswer))
                .thenReturn(processRunner);
        Mockito.when(testRunnerServiceMock.condense(processRunner))
                .thenReturn(resultCondenser);
        Mockito.when(challengeRepositoryMock.findById(challengeId))
                .thenReturn(Optional.of(challenge));
        Mockito.when(userAccountRepositoryMock.findById(userId))
                .thenReturn(Optional.of(user));

        // Act

        challengeConsumer.receiveChallenger(challengeAnswerId);

        // Assert

        Mockito.verify(challenge, Mockito.times(1))
                .addToRank(challengeAnswer);
        Mockito.verify(user, Mockito.times(1))
                .updateScore(challenge, challengeAnswer);

        Mockito.verify(challengeRepositoryMock, Mockito.times(1))
                .save(challenge);
        Mockito.verify(userAccountRepositoryMock, Mockito.times(1))
                .save(user);
    }
}
