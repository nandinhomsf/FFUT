package br.com.eterniaserver.ffut.domain.challenge.models;

import br.com.eterniaserver.ffut.domain.challenge.enums.ChallengeDifficulty;

public record CreateChallengeResponse(String id, String name, String description, Integer challengeVersion, String code, ChallengeDifficulty difficulty) {
}
