package br.com.eterniaserver.ffut.domain.challenge.services;

import br.com.eterniaserver.ffut.Constants;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.MutationResultEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.ChallengeResultEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.LineResultEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeEntity;
import br.com.eterniaserver.ffut.domain.challenge.enums.AnswerStatus;
import br.com.eterniaserver.ffut.domain.challenge.models.*;
import br.com.eterniaserver.ffut.domain.challenge.models.ReadChallengeAnswerResponse.ReadMutationResultResponse;
import br.com.eterniaserver.ffut.domain.challenge.models.ReadChallengeAnswerResponse.ReadChallengeResultResponse;
import br.com.eterniaserver.ffut.domain.challenge.models.ReadChallengeAnswerResponse.ReadLineResultResponse;
import br.com.eterniaserver.ffut.domain.challenge.queue.ChallengeProducer;
import br.com.eterniaserver.ffut.domain.challenge.repositories.ChallengeAnswerRepository;
import br.com.eterniaserver.ffut.domain.challenge.repositories.ChallengeRepository;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChallengeAnswerService {

    private final ChallengeRepository challengeRepository;

    private final ChallengeAnswerRepository answerRepository;

    private final ChallengeProducer challengeProducer;

    @Transactional
    public ListChallengeAnswerResponse listByUser(int page, int size, String userId) {
        return new ListChallengeAnswerResponse(
                answerRepository
                        .findAllByUserIdOrderByCreatedAtDesc(PageRequest.of(page, size), userId)
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                challengeRepository.count()
        );
    }

    @Transactional
    public ListChallengeAnswerResponse list(ListChallengeAnswerRequest request) {
        return new ListChallengeAnswerResponse(
                answerRepository
                        .findAllByChallengeIdAndUserIdOrderByCreatedAtDesc(request.challengeId(), request.userId())
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                answerRepository.count()
        );
    }

    @Transactional
    public CreateChallengeAnswerResponse create(CreateChallengeAnswerRequest request) {
        ChallengeEntity challenge = getChallengeById(request.challengeId());

        ChallengeAnswerEntity entity = new ChallengeAnswerEntity();

        entity.setChallengeId(request.challengeId());
        entity.setChallengeName(challenge.getName());
        entity.setUserId(request.userId());
        entity.setUsername(request.username());
        entity.setStatus(AnswerStatus.PENDING);
        entity.setChallengeVersion(challenge.getChallengeVersion());
        entity.setChallengeCode(challenge.getCode());
        entity.setUserTestCode(request.testAnswer());
        entity.setCreatedAt(new Date(System.currentTimeMillis()));

        answerRepository.save(entity);

        challengeProducer.sendChallengeToWorker(entity.getId());

        return new CreateChallengeAnswerResponse(entity.getId(), entity.getStatus());
    }

    @Transactional
    public ReadChallengeAnswerResponse read(String id) {
        ChallengeAnswerEntity entity = getById(id);

        return toResponse(entity);
    }

    private ReadChallengeAnswerResponse toResponse(ChallengeAnswerEntity entity) {
        return new ReadChallengeAnswerResponse(
                entity.getId(),
                entity.getChallengeVersion(),
                entity.getChallengeId(),
                entity.getChallengeName(),
                entity.getChallengeCode(),
                entity.getUserTestCode(),
                entity.getStatus(),
                entity.getCreatedAt(),
                toResponse(entity.getChallengeResult())
        );
    }

    private Optional<ReadChallengeResultResponse> toResponse(Optional<ChallengeResultEntity> optional) {
        if (optional.isEmpty()) {
            return Optional.empty();
        }

        ChallengeResultEntity entity = optional.get();

        return Optional.of(
                new ReadChallengeResultResponse(
                        entity.getScore(),
                        entity.getTestsSuccess(),
                        entity.getTestsFailed(),
                        entity.getTestsError(),
                        entity.getInstructionCoverage(),
                        entity.getInstructionMissed(),
                        entity.getBranchCoverage(),
                        entity.getBranchMissed(),
                        entity.getLineCoverage(),
                        entity.getLineMissed(),
                        entity.getComplexityCoverage(),
                        entity.getComplexityMissed(),
                        entity.getMethodCoverage(),
                        entity.getMethodMissed(),
                        entity.getLineResults().stream().map(this::toResponse).toList(),
                        entity.getMutationResults().stream().map(this::toResponse).toList()
                )
        );
    }

    private ReadLineResultResponse toResponse(LineResultEntity entity) {
        return new ReadLineResultResponse(
                entity.getLineNumber(),
                entity.getInstructionMissed(),
                entity.getInstructionCoverage(),
                entity.getBranchMissed(),
                entity.getBranchCoverage(),
                entity.isCovered()
        );
    }

    private ReadMutationResultResponse toResponse(MutationResultEntity entity) {
        return new ReadMutationResultResponse(
                entity.getMutationType(),
                entity.getMutationInfo(),
                entity.getIsKilled(),
                entity.getLine()
        );
    }

    private ChallengeAnswerEntity getById(String id) {
        return answerRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Constants.ANSWER_NOT_FOUND));
    }

    private ChallengeEntity getChallengeById(String id) {
        return challengeRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Constants.CHALLENGE_NOT_FOUND));
    }

}
