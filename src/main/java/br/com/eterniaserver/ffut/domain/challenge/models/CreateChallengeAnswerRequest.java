package br.com.eterniaserver.ffut.domain.challenge.models;

public record CreateChallengeAnswerRequest(String challengeId, String userId, String username, String testAnswer) {
}
