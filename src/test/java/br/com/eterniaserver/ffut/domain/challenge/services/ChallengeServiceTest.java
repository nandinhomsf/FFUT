package br.com.eterniaserver.ffut.domain.challenge.services;

import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeEntity;
import br.com.eterniaserver.ffut.domain.challenge.enums.ChallengeDifficulty;
import br.com.eterniaserver.ffut.domain.challenge.models.CreateChallengeRequest;
import br.com.eterniaserver.ffut.domain.challenge.models.ListChallengeResponse;
import br.com.eterniaserver.ffut.domain.challenge.models.ReadChallengeResponse;
import br.com.eterniaserver.ffut.domain.challenge.models.UpdateChallengeRequest;
import br.com.eterniaserver.ffut.domain.challenge.repositories.ChallengeRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

class ChallengeServiceTest {

    private ChallengeRepository repository;
    private ChallengeService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(ChallengeRepository.class);
        service = new ChallengeService(repository);
    }

    @Test
    void testListReturnAll() {
        // Arrange
        ChallengeEntity first = new ChallengeEntity();
        first.setId("#FIRST1");
        first.setName("First");
        first.setDescription("First description");
        first.setCode("First code");

        ChallengeEntity second = new ChallengeEntity();
        second.setId("#SECOND2");
        second.setName("Second");
        second.setDescription("Second description");
        second.setCode("Second code");

        List<ChallengeEntity> challengeEntities = List.of(first, second);

        Pageable pageRequest = Mockito.any(Pageable.class);
        Page<ChallengeEntity> page = new PageImpl<>(challengeEntities);

        Mockito.when(repository.findAll(pageRequest)).thenReturn(page);

        // Act
        ListChallengeResponse result = service.list(0, 10);

        List<ReadChallengeResponse> list = result.challenges();

        // Assert
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals("First", list.getFirst().name());
        Assertions.assertEquals("First description", list.getFirst().description());
        Assertions.assertEquals("First code", list.getFirst().code());
        Assertions.assertEquals("Second", list.getLast().name());
        Assertions.assertEquals("Second description", list.getLast().description());
        Assertions.assertEquals("Second code", list.getLast().code());
    }

    @Test
    void testCreate() {
        // Arrange
        CreateChallengeRequest challengeDto = new CreateChallengeRequest("First", "First description", "First code", ChallengeDifficulty.EASY, null);

        ChallengeEntity challengeEntity = new ChallengeEntity();
        challengeEntity.setName("First");
        challengeEntity.setDescription("First description");
        challengeEntity.setCode("First code");
        challengeEntity.setChallengeVersion(0);
        challengeEntity.setDifficulty(ChallengeDifficulty.EASY);

        // Act
        service.create(challengeDto);

        // Assert
        Mockito.verify(repository).save(challengeEntity);
    }

    @Test
    void testRead() {
        // Arrange
        ChallengeEntity entity = new ChallengeEntity();
        entity.setId("#FIRST1");
        entity.setName("First");
        entity.setDescription("First description");
        entity.setCode("First code");

        Mockito.when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        // Act
        ReadChallengeResponse result = service.read("#FIRST1");

        // Assert
        Assertions.assertEquals("First", result.name());
        Assertions.assertEquals("First description", result.description());
        Assertions.assertEquals("First code", result.code());
    }

    @Test
    void testReadNotFound() {
        // Arrange
        Mockito.when(repository.findById("#FIRST1")).thenReturn(Optional.empty());

        // Act and Assert
        Assertions.assertThrows(ResponseStatusException.class, () -> service.read("#FIRST1"));
    }

    @Test
    void testUpdate() {
        // Arrange
        UpdateChallengeRequest challengeDto = new UpdateChallengeRequest(
                "First",
                "First description",
                "First code",
                ChallengeDifficulty.EASY,
                null
        );

        ChallengeEntity entity = new ChallengeEntity();
        entity.setId("#FIRST1");
        entity.setName("First");
        entity.setDescription("First description");
        entity.setCode("First code");
        entity.setChallengeVersion(0);
        entity.setDifficulty(ChallengeDifficulty.EASY);

        Mockito.when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        // Act
        service.update("#FIRST1", challengeDto);

        // Assert
        Mockito.verify(repository).save(entity);
    }

    @Test
    void testUpdateNotFound() {
        // Arrange
        UpdateChallengeRequest challengeDto = new UpdateChallengeRequest(
                "First",
                "First description",
                "First code",
                ChallengeDifficulty.EASY,
                null
        );

        Mockito.when(repository.findById("#FIRST1")).thenReturn(Optional.empty());

        // Act and Assert
        Assertions.assertThrows(ResponseStatusException.class, () -> service.update("#FIRST1", challengeDto));
    }

    @Test
    void testDelete() {
        // Arrange
        ChallengeEntity entity = new ChallengeEntity();
        entity.setId("#FIRST1");
        entity.setName("First");
        entity.setDescription("First description");
        entity.setCode("First code");

        Mockito.when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        // Act
        service.delete("#FIRST1");

        // Assert
        Mockito.verify(repository).delete(entity);
    }

    @Test
    void testDeleteNotFound() {
        // Arrange
        Mockito.when(repository.findById("#FIRST1")).thenReturn(Optional.empty());

        // Act and Assert
        Assertions.assertThrows(ResponseStatusException.class, () -> service.delete("#FIRST1"));
    }

    private static final String HIDDEN = "HIDDEN_REFERENCE_SENTINEL_7f3a";

    private ChallengeEntity existingWithReference() {
        ChallengeEntity entity = new ChallengeEntity();
        entity.setId("#FIRST1");
        entity.setName("First");
        entity.setCode("Shown code");
        entity.setChallengeVersion(0);
        entity.setEvaluationCode(HIDDEN);
        Mockito.when(repository.findById("#FIRST1")).thenReturn(Optional.of(entity));
        return entity;
    }

    private ChallengeEntity savedEntity() {
        ArgumentCaptor<ChallengeEntity> captor = ArgumentCaptor.forClass(ChallengeEntity.class);
        Mockito.verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void testCreateStoresEvaluationCode() {
        service.create(new CreateChallengeRequest("First", "Desc", "Shown code", ChallengeDifficulty.EASY, HIDDEN));

        Assertions.assertEquals(HIDDEN, savedEntity().getEvaluationCode());
        Assertions.assertEquals("Shown code", savedEntity().getCode());
    }

    @Test
    void testCreateBlankEvaluationCodeBecomesNull() {
        service.create(new CreateChallengeRequest("First", "Desc", "Shown code", ChallengeDifficulty.EASY, "   "));

        Assertions.assertNull(savedEntity().getEvaluationCode());
        Assertions.assertFalse(savedEntity().hasEvaluationCode());
    }

    @Test
    void testUpdateNullEvaluationCodeKeepsReference() {
        existingWithReference();

        service.update("#FIRST1", new UpdateChallengeRequest("First", "Desc", "New code", ChallengeDifficulty.EASY, null));

        Assertions.assertEquals(HIDDEN, savedEntity().getEvaluationCode());
        Assertions.assertEquals("New code", savedEntity().getCode());
    }

    @Test
    void testUpdateEmptyEvaluationCodeRemovesReference() {
        existingWithReference();

        service.update("#FIRST1", new UpdateChallengeRequest("First", "Desc", "New code", ChallengeDifficulty.EASY, ""));

        Assertions.assertNull(savedEntity().getEvaluationCode());
    }

    @Test
    void testUpdateReplacesEvaluationCode() {
        existingWithReference();

        service.update("#FIRST1", new UpdateChallengeRequest("First", "Desc", "New code", ChallengeDifficulty.EASY, "Other reference"));

        Assertions.assertEquals("Other reference", savedEntity().getEvaluationCode());
    }

    @Test
    void testReadAndListNeverExposeEvaluationCode() throws Exception {
        ChallengeEntity entity = existingWithReference();
        Mockito.when(repository.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        ObjectMapper mapper = new ObjectMapper();
        String read = mapper.writeValueAsString(service.read("#FIRST1"));
        String list = mapper.writeValueAsString(service.list(0, 10));

        Assertions.assertFalse(read.contains(HIDDEN));
        Assertions.assertFalse(list.contains(HIDDEN));
        Assertions.assertTrue(read.contains("Shown code"));
    }
}
