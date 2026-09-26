package br.com.eterniaserver.ffut.domain.challenge.entities;

import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeEntity.ChallengeRankEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.ChallengeResultEntity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ChallengeEntityTest {

    private ChallengeEntity challengeEntity;

    @BeforeEach
    void setUp() {
        challengeEntity = new ChallengeEntity();
    }

    @Test
    void shouldSortWhenAddToRanking() {
        // Arrange

        setChallengeRanks();

        ChallengeAnswerEntity newAnswer = new ChallengeAnswerEntity();
        ChallengeResultEntity newResult = new ChallengeResultEntity();
        
        newResult.setScore(100.0D);
        
        newAnswer.setChallengeResult(newResult);
        newAnswer.setUserId("123");
        newAnswer.setUsername("test");

        // Act

        challengeEntity.addToRank(newAnswer);

        // Assert

        List<ChallengeRankEntity> rank = challengeEntity.getRank();

        Assertions.assertEquals(3, rank.size());

        Assertions.assertEquals(100.0D, rank.getFirst().getChallengeResultEntity().getScore());
        Assertions.assertEquals(75.0D, rank.get(1).getChallengeResultEntity().getScore());
        Assertions.assertEquals(50.0D, rank.getLast().getChallengeResultEntity().getScore());
    }

    @Test
    void shouldReplaceOldRankingWhenSort() {
        // Arrange

        setChallengeRanks();

        ChallengeAnswerEntity alreadyAnsweredNewResult = new ChallengeAnswerEntity();
        ChallengeResultEntity newResult = new ChallengeResultEntity();

        newResult.setScore(51.5D);

        alreadyAnsweredNewResult.setChallengeResult(newResult);
        alreadyAnsweredNewResult.setUserId("789");
        alreadyAnsweredNewResult.setUsername("test");

        // Act

        challengeEntity.addToRank(alreadyAnsweredNewResult);

        // Assert

        List<ChallengeRankEntity> rank = challengeEntity.getRank();

        Assertions.assertEquals(2, rank.size());

        Assertions.assertEquals(75.0D, rank.getFirst().getChallengeResultEntity().getScore());
        Assertions.assertEquals(51.5D, rank.getLast().getChallengeResultEntity().getScore());
        Assertions.assertEquals("test", rank.getLast().getUsername());
    }

    private void setChallengeRanks() {
        ChallengeRankEntity oldFirst = new ChallengeRankEntity();
        ChallengeResultEntity oldFirstResult = new ChallengeResultEntity();

        oldFirstResult.setScore(75.0D);

        oldFirst.setUserId("456");
        oldFirst.setChallengeResultEntity(oldFirstResult);

        ChallengeRankEntity oldSecond = new ChallengeRankEntity();
        ChallengeResultEntity oldSecondResult = new ChallengeResultEntity();

        oldSecondResult.setScore(50.0D);

        oldSecond.setUserId("789");
        oldSecond.setChallengeResultEntity(oldSecondResult);

        challengeEntity.setRank(new ArrayList<>(List.of(oldFirst, oldSecond)));
    }

}
