package br.com.eterniaserver.ffut.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum BaseRoles {

    USER("label.user"),
    ADMIN("label.admin");

    private final String label;

    public static Optional<BaseRoles> fromString(String name) {
        return Arrays
                .stream(BaseRoles.values())
                .filter(role -> role.name().equals(name))
                .findFirst();
    }

}
