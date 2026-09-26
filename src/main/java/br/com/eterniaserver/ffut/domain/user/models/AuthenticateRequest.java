package br.com.eterniaserver.ffut.domain.user.models;

import br.com.eterniaserver.ffut.Constants;
import br.com.eterniaserver.ffut.domain.user.entities.UserAccountEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthenticateRequest {

    @NotNull(message = Constants.LOGIN_NEEDED)
    private final String login;

    @NotNull(message = Constants.PASSWORD_NEEDED)
    private final String password;

    @JsonIgnore
    private UserAccountEntity userAccountEntity;

    private AuthenticateRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

}
