package br.com.eterniaserver.ffut.domain.challenge.models;

import java.util.List;

public record ListChallengeAnswerResponse(List<ReadChallengeAnswerResponse> answers, Long total) {
}
