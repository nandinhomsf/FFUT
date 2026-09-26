package br.com.eterniaserver.ffut.domain.challenge.services;

import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.MutationResultEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.ChallengeResultEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeEntity;
import br.com.eterniaserver.ffut.domain.challenge.enums.AnswerStatus;
import br.com.eterniaserver.ffut.domain.challenge.models.CreateChallengeAnswerRequest;
import br.com.eterniaserver.ffut.domain.challenge.models.ListChallengeAnswerRequest;
import br.com.eterniaserver.ffut.domain.challenge.models.ListChallengeAnswerResponse;
import br.com.eterniaserver.ffut.domain.challenge.models.ReadChallengeAnswerResponse;
import br.com.eterniaserver.ffut.domain.challenge.queue.ChallengeProducer;
import br.com.eterniaserver.ffut.domain.challenge.repositories.ChallengeAnswerRepository;
import br.com.eterniaserver.ffut.domain.challenge.repositories.ChallengeRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

class ChallengeAnswerServiceTest {

    private ChallengeAnswerRepository answerRepository;
    private ChallengeRepository challengeRepository;
    private ChallengeProducer producerService;

    private ChallengeAnswerService service;

    @BeforeEach
    void setUp() {
        producerService = Mockito.mock(ChallengeProducer.class);
        answerRepository = Mockito.mock(ChallengeAnswerRepository.class);
        challengeRepository = Mockito.mock(ChallengeRepository.class);

        service = new ChallengeAnswerService(
                challengeRepository,
                answerRepository,
                producerService
        );
    }

    @Test
    void testListReturnAllByUser() {
        // Arrange
        String userId = "#USER1";
        String challengeId = "#CHALLENGE1";

        ChallengeEntity challenge = new ChallengeEntity();
        challenge.setId(challengeId);
        challenge.setName("Challenge");
        challenge.setDescription("Challenge description");
        challenge.setCode("Challenge code");
        challenge.setChallengeVersion(1);

        Mockito.when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));

        ChallengeAnswerEntity first = new ChallengeAnswerEntity();
        first.setId("#FIRST1");
        first.setChallengeId("#CHALLENGE1");
        first.setUserId(userId);
        first.setUsername("User");
        first.setStatus(AnswerStatus.CORRECT);
        first.setChallengeVersion(challenge.getChallengeVersion());

        ChallengeAnswerEntity second = new ChallengeAnswerEntity();
        second.setId("#SECOND2");
        second.setChallengeId("#CHALLENGE1");
        second.setUserId(userId);
        second.setUsername("User");
        second.setStatus(AnswerStatus.INCORRECT);

        List<ChallengeAnswerEntity> challengeAnswerEntities = List.of(first, second);

        Mockito.when(answerRepository.findAllByUserIdOrderByCreatedAtDesc(PageRequest.of(0, 10), userId))
                .thenReturn(challengeAnswerEntities);

        // Act
        ListChallengeAnswerResponse result = service.listByUser(0, 10, userId);

        // Assert
        Assertions.assertEquals(2, result.answers().size());
        Assertions.assertEquals(AnswerStatus.CORRECT, result.answers().getFirst().status());
        Assertions.assertEquals(AnswerStatus.INCORRECT, result.answers().getLast().status());
    }

    @Test
    void testListReturnAll() {
        // Arrange
        String userId = "#USER1";
        String challengeId = "#CHALLENGE1";

        ChallengeEntity challenge = new ChallengeEntity();
        challenge.setId(challengeId);
        challenge.setName("Challenge");
        challenge.setDescription("Challenge description");
        challenge.setCode("Challenge code");
        challenge.setChallengeVersion(1);

        Mockito.when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));

        ChallengeAnswerEntity first = new ChallengeAnswerEntity();
        first.setId("#FIRST1");
        first.setChallengeId("#CHALLENGE1");
        first.setUserId(userId);
        first.setUsername("User");
        first.setStatus(AnswerStatus.CORRECT);
        first.setChallengeVersion(challenge.getChallengeVersion());

        ChallengeAnswerEntity second = new ChallengeAnswerEntity();
        second.setId("#SECOND2");
        second.setChallengeId("#CHALLENGE1");
        second.setUserId(userId);
        second.setUsername("User");
        second.setStatus(AnswerStatus.INCORRECT);

        List<ChallengeAnswerEntity> challengeAnswerEntities = List.of(first, second);

        Mockito.when(answerRepository.findAllByChallengeIdAndUserIdOrderByCreatedAtDesc(challengeId, userId))
                .thenReturn(challengeAnswerEntities);

        // Act
        ListChallengeAnswerRequest request = new ListChallengeAnswerRequest(challengeId, userId);

        ListChallengeAnswerResponse result = service.list(request);

        // Assert
        Assertions.assertEquals(2, result.answers().size());
        Assertions.assertEquals(AnswerStatus.CORRECT, result.answers().getFirst().status());
        Assertions.assertEquals(AnswerStatus.INCORRECT, result.answers().getLast().status());
    }

    @Test
    void testCreate() {
        // Arrange
        String userId = "#USER1";
        String answerId = "#ANSWER1";
        String challengeId = "#CHALLENGE1";
        String username = "User";
        String testAnswer = "Test answer";

        ChallengeEntity challenge = new ChallengeEntity();
        challenge.setId(challengeId);
        challenge.setName("Challenge");
        challenge.setDescription("Challenge description");
        challenge.setCode("Challenge code");
        challenge.setChallengeVersion(1);

        Mockito.when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));

        Mockito.when(answerRepository.save(Mockito.any(ChallengeAnswerEntity.class)))
                .thenAnswer(invocation -> {
                    ChallengeAnswerEntity entity = invocation.getArgument(0);
                    entity.setId(answerId);
                    return entity;
                });

        // Act

        CreateChallengeAnswerRequest request = new CreateChallengeAnswerRequest(
                challengeId,
                userId,
                username,
                testAnswer
        );

        service.create(request);

        // Assert

        Mockito.verify(answerRepository, Mockito.times(1))
                .save(
                        ArgumentMatchers.argThat(x -> challengeId.equals(x.getChallengeId()) &&
                                userId.equals(x.getUserId()) &&
                                username.equals(x.getUsername()) &&
                                testAnswer.equals(x.getUserTestCode()) &&
                                AnswerStatus.PENDING.equals(x.getStatus()) &&
                                challenge.getChallengeVersion().equals(x.getChallengeVersion()) &&
                                challenge.getCode().equals(x.getChallengeCode())
                        )
                );

        Mockito.verify(producerService, Mockito.times(1))
                .sendChallengeToWorker(answerId);
    }

    @Test
    void testRead() {
        // Arrange
        String challengeAnswerId = "#ANSWER1";
        String userId = "#USER1";
        String challengeId = "#CHALLENGE1";

        MutationResultEntity mutationResult = Mockito.mock(MutationResultEntity.class);

        ChallengeResultEntity challengeResult = Mockito.mock(ChallengeResultEntity.class);

        Mockito.when(challengeResult.getMutationResults())
                .thenReturn(List.of(mutationResult));
        Mockito.when(challengeResult.getScore())
                .thenReturn(100.0);

        ChallengeAnswerEntity challengeAnswerEntity = new ChallengeAnswerEntity();
        challengeAnswerEntity.setId(challengeAnswerId);
        challengeAnswerEntity.setChallengeId(challengeId);
        challengeAnswerEntity.setUserId(userId);
        challengeAnswerEntity.setUsername("User");
        challengeAnswerEntity.setStatus(AnswerStatus.CORRECT);
        challengeAnswerEntity.setChallengeVersion(1);
        challengeAnswerEntity.setChallengeCode("Challenge code");
        challengeAnswerEntity.setUserTestCode("Test answer");
        challengeAnswerEntity.setChallengeResult(challengeResult);

        Mockito.when(answerRepository.findById(challengeAnswerId))
                .thenReturn(Optional.of(challengeAnswerEntity));

        // Act
        ReadChallengeAnswerResponse result = service.read(challengeAnswerId);

        // Assert
        Assertions.assertEquals(challengeAnswerId, result.answerId());
        Assertions.assertTrue(result.challengeResult().isPresent());
        Assertions.assertEquals(1, result.challengeResult().get().mutationResults().size());
        Assertions.assertEquals(100.0, result.challengeResult().get().score());
    }
}
