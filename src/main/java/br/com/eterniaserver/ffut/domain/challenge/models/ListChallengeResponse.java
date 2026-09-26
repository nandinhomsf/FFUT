package br.com.eterniaserver.ffut.domain.challenge.models;

import java.util.List;

public record ListChallengeResponse(List<ReadChallengeResponse> challenges, long totalEntries) {
}
