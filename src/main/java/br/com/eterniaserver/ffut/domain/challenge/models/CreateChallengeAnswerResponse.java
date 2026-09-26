package br.com.eterniaserver.ffut.domain.challenge.models;

import br.com.eterniaserver.ffut.domain.challenge.enums.AnswerStatus;

public record CreateChallengeAnswerResponse(String id, AnswerStatus status) {
}
