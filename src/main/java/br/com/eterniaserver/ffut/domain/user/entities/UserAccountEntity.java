package br.com.eterniaserver.ffut.domain.user.entities;

import br.com.eterniaserver.ffut.Constants;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.ChallengeResultEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeEntity;
import br.com.eterniaserver.ffut.domain.challenge.enums.ChallengeDifficulty;
import br.com.eterniaserver.ffut.domain.user.dtos.UserDto;
import br.com.eterniaserver.ffut.domain.user.enums.BaseLocales;
import br.com.eterniaserver.ffut.domain.user.enums.BaseRoles;
import br.com.eterniaserver.ffut.domain.user.models.ListUserRankResponse.UserRankResponse;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Data
@Document(collection = "users")
public class UserAccountEntity {

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,8}$");

    @Id
    private String id;

    @Indexed(unique = true)
    private String login;

    private String password;

    private String name;

    private String surname;

    private String locale;

    private LocalDateTime createdAt;

    private Boolean verified = false;

    private Boolean active = true;

    private Double score = 0.0D;

    private Map<String, Pair<ChallengeDifficulty, Double>> challengesSolved = new HashMap<>();

    private List<String> roles = new ArrayList<>();

    public String getUsername() {
        return name + " " + surname;
    }

    public UserRankResponse toRank(int index) {
        return new UserRankResponse(index, getUsername(), score);
    }

    public void validate() {
        if (roles.stream().anyMatch(role -> BaseRoles.fromString(role).isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.INVALID_ROLE);
        }

        if (BaseLocales.fromString(locale).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.INVALID_LOCALE);
        }

        if (!EMAIL_REGEX.matcher(login).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.INVALID_LOGIN);
        }
    }

    public void addRole(BaseRoles role) {
        roles.add(role.name());
    }

    public void updateScore(ChallengeEntity challenge, ChallengeAnswerEntity answer) {
        Optional<ChallengeResultEntity> resultOptional = answer.getChallengeResult();
        if (resultOptional.isEmpty()) {
            return;
        }

        ChallengeResultEntity result = resultOptional.get();
        challengesSolved.put(answer.getChallengeId(), Pair.of(challenge.getDifficulty(), result.getScore()));

        score = challengesSolved
                .values()
                .stream()
                .mapToDouble(pair -> pair.getSecond() * pair.getFirst().getMultiplier())
                .sum();
    }

    public UserDto toDto() {
        return new UserDto(
                getLogin(),
                getId(),
                getUsername(),
                getName(),
                getSurname(),
                getRoles(),
                getCreatedAt()
        );
    }

}
