package br.com.eterniaserver.ffut.domain.challenge.models;

import br.com.eterniaserver.ffut.domain.challenge.enums.AnswerStatus;
import br.com.eterniaserver.ffut.domain.challenge.enums.MutationType;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public record ReadChallengeAnswerResponse(
        String answerId,
        Integer challengeVersion,
        String challengeId,
        String challengeName,
        String challengeCode,
        String userTest,
        AnswerStatus status,
        Date createdAt,
        Optional<ReadChallengeResultResponse> challengeResult) {

    public record ReadChallengeResultResponse(
            Double score,
            Integer testsSuccess,
            Integer testsFailed,
            Integer testsError,
            Integer instructionCoverage,
            Integer instructionMissed,
            Integer branchCoverage,
            Integer branchMissed,
            Integer lineCoverage,
            Integer lineMissed,
            Integer complexityCoverage,
            Integer complexityMissed,
            Integer methodCoverage,
            Integer methodMissed,
            List<ReadLineResultResponse> lineResults,
            List<ReadMutationResultResponse> mutationResults) {
    }

    public record ReadMutationResultResponse(
            MutationType mutationType,
            String mutationInfo,
            Boolean isKilled,
            Integer line) {
    }

    public record ReadLineResultResponse(
            Integer lineNumber,
            Integer instructionMissed,
            Integer instructionCoverage,
            Integer branchMissed,
            Integer branchCoverage,
            Boolean covered) {
    }

}
