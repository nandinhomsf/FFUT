package br.com.eterniaserver.ffut.domain.challenge.entities;

import br.com.eterniaserver.ffut.domain.challenge.enums.AnswerStatus;
import br.com.eterniaserver.ffut.domain.challenge.enums.MutationType;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
@Document(collection = "answers")
public class ChallengeAnswerEntity {

    @Id
    private String id;

    private String challengeId;

    private String challengeName;

    private String userId;

    private String username;

    private AnswerStatus status;

    private Integer challengeVersion;

    private String challengeCode;

    private String userTestCode;

    private ChallengeResultEntity challengeResult;

    private Date createdAt;

    public Optional<ChallengeResultEntity> getChallengeResult() {
        return Optional.ofNullable(challengeResult);
    }

    @Data
    public static class ChallengeResultEntity {

        private Double score = 0.0D;

        private Integer testsSuccess = 0;

        private Integer testsFailed = 0;

        private Integer testsError = 0;

        private Integer instructionCoverage = 0;

        private Integer instructionMissed = 0;

        private Integer branchCoverage = 0;

        private Integer branchMissed = 0;

        private Integer lineCoverage = 0;

        private Integer lineMissed = 0;

        private Integer complexityCoverage = 0;

        private Integer complexityMissed = 0;

        private Integer methodCoverage = 0;

        private Integer methodMissed = 0;

        private List<LineResultEntity> lineResults = new ArrayList<>();

        private List<MutationResultEntity> mutationResults = new ArrayList<>();

    }

    @Data
    public static class MutationResultEntity {

        private MutationType mutationType;

        private String mutationInfo;

        private Boolean isKilled;

        private Integer line;
    }

    @Data
    public static class LineResultEntity {

        private Integer lineNumber;

        private Integer instructionMissed;

        private Integer instructionCoverage;

        private Integer branchMissed;

        private Integer branchCoverage;

        public boolean isCovered() {
            return (instructionCoverage > 0 || (instructionCoverage == 0 && instructionMissed == 0))
                    || (branchCoverage > 0 || (branchCoverage == 0 && branchMissed == 0));
        }

    }

}
