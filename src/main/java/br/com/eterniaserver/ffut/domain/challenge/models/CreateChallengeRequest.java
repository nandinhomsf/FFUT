package br.com.eterniaserver.ffut.domain.challenge.models;

import br.com.eterniaserver.ffut.domain.challenge.enums.ChallengeDifficulty;

public record CreateChallengeRequest(String name, String description, String code, ChallengeDifficulty difficulty) {
}
