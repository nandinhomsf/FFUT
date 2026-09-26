package br.com.eterniaserver.ffut.domain.user.models;

import br.com.eterniaserver.ffut.Constants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
public class UpdateUserRequest {

    @NotNull(message = Constants.LOGIN_NEEDED)
    private final String login;

    @NotNull(message = Constants.NAME_NEEDED)
    private final String name;

    @NotNull(message = Constants.SURNAME_NEEDED)
    private final String surname;

    @JsonIgnore
    private UserDetails userDetails;

    private UpdateUserRequest(String login, String name, String surname) {
        this.login = login;
        this.name = name;
        this.surname = surname;
    }

}
