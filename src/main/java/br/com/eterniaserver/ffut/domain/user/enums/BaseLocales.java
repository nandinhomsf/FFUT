package br.com.eterniaserver.ffut.domain.user.enums;

import java.util.Arrays;
import java.util.Optional;

public enum BaseLocales {

    PORTUGUESE,
    ENGLISH;

    public static Optional<BaseLocales> fromString(String name) {
        return Arrays
                .stream(BaseLocales.values())
                .filter(locale -> locale.name().equals(name))
                .findFirst();
    }

}