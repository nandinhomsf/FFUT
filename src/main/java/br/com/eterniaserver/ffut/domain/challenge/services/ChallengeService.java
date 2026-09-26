package br.com.eterniaserver.ffut.domain.challenge.services;

import br.com.eterniaserver.ffut.Constants;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeEntity.ChallengeRankEntity;
import br.com.eterniaserver.ffut.domain.challenge.models.CreateChallengeRequest;
import br.com.eterniaserver.ffut.domain.challenge.models.ListChallengeResponse;
import br.com.eterniaserver.ffut.domain.challenge.models.ReadChallengeResponse;
import br.com.eterniaserver.ffut.domain.challenge.models.ReadChallengeResponse.ReadChallengeRankResponse;
import br.com.eterniaserver.ffut.domain.challenge.models.UpdateChallengeRequest;
import br.com.eterniaserver.ffut.domain.challenge.repositories.ChallengeRepository;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    @Transactional
    public ListChallengeResponse list(int page, int size) {
        return new ListChallengeResponse(
                challengeRepository
                        .findAll(PageRequest.of(page, size))
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                challengeRepository.count()
        );
    }

    @Transactional
    public ChallengeEntity create(CreateChallengeRequest request) {
        ChallengeEntity entity = new ChallengeEntity();

        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setCode(request.code());
        entity.setChallengeVersion(0);
        entity.setDifficulty(request.difficulty());

        challengeRepository.save(entity);

        return entity;
    }

    @Transactional
    public ReadChallengeResponse read(String id) {
        ChallengeEntity entity = getById(id);

        return toResponse(entity);
    }

    @Transactional
    public void update(String id, UpdateChallengeRequest data) {
        ChallengeEntity entity = getById(id);

        entity.setName(data.name());
        entity.setDescription(data.description());
        entity.setCode(data.code());
        entity.setDifficulty(data.difficulty());

        entity.incrementChallengeVersion();

        challengeRepository.save(entity);
    }

    @Transactional
    public void delete(String id) {
        ChallengeEntity entity = getById(id);

        challengeRepository.delete(entity);
    }

    private ChallengeEntity getById(String id) {
        return challengeRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Constants.CHALLENGE_NOT_FOUND));
    }

    private ReadChallengeResponse toResponse(ChallengeEntity entity) {
        return new ReadChallengeResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getChallengeVersion(),
                entity.getCode(),
                entity.getDifficulty(),
                entity.getRank().stream().map(this::toResponse).toList()
        );
    }

    private ReadChallengeRankResponse toResponse(ChallengeRankEntity rankEntity) {
        return new ReadChallengeRankResponse(
                rankEntity.getUserId(),
                rankEntity.getUsername(),
                rankEntity.getChallengeResultEntity().getScore()
        );
    }
}
