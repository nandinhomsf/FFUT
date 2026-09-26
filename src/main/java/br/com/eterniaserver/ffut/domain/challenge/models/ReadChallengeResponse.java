package br.com.eterniaserver.ffut.domain.challenge.models;

import br.com.eterniaserver.ffut.domain.challenge.enums.ChallengeDifficulty;

import java.util.List;

public record ReadChallengeResponse(String id, String name, String description, Integer challengeVersion, String code, ChallengeDifficulty difficulty, List<ReadChallengeRankResponse> rank) {

    public record ReadChallengeRankResponse(String userId, String username, Double score) {
    }

}
