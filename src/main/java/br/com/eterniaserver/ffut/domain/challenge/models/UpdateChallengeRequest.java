package br.com.eterniaserver.ffut.domain.challenge.models;

import br.com.eterniaserver.ffut.domain.challenge.enums.ChallengeDifficulty;

// evaluationCode nulo mantém a referência atual; string vazia remove.
public record UpdateChallengeRequest(String name, String description, String code, ChallengeDifficulty difficulty, String evaluationCode) {
}
