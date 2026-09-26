package br.com.eterniaserver.ffut.domain.challenge.entities;

import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.ChallengeResultEntity;

import br.com.eterniaserver.ffut.domain.challenge.enums.ChallengeDifficulty;
import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Data
@Document(collection = "challenge")
public class ChallengeEntity {

    @Id
    private String id;

    private String name;

    private String description;

    private Integer challengeVersion;

    private String code;

    private ChallengeDifficulty difficulty;

    private List<ChallengeRankEntity> rank = new ArrayList<>();

    public void incrementChallengeVersion() {
        challengeVersion++;
    }

    public void addToRank(ChallengeAnswerEntity answer) {
        Optional<ChallengeResultEntity> resultOptional = answer.getChallengeResult();
        if (resultOptional.isPresent()) {
            ChallengeResultEntity result = resultOptional.get();

            ChallengeRankEntity challengeRankEntity = new ChallengeRankEntity();

            challengeRankEntity.setUserId(answer.getUserId());
            challengeRankEntity.setUsername(answer.getUsername());
            challengeRankEntity.setChallengeResultEntity(result);

            rank.removeIf(entity -> answer.getUserId().equals(entity.getUserId()));
            rank.add(challengeRankEntity);

            Comparator<ChallengeRankEntity> comparator = Comparator.comparingDouble(o -> o.getChallengeResultEntity().getScore());

            rank.sort(comparator.reversed());
        }
    }

    @Data
    public static class ChallengeRankEntity {

        private String userId;

        private String username;

        private ChallengeResultEntity challengeResultEntity;
    }

}
