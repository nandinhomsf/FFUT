package br.com.eterniaserver.ffut.domain.challenge.enums;

public enum ChallengeDifficulty {

    EASY,
    NORMAL,
    MEDIUM,
    HARD;

    public double getMultiplier() {
        return switch (this) {
            case EASY -> 0.05;
            case NORMAL -> 0.15;
            case MEDIUM -> 0.2;
            case HARD -> 0.25;
        };
    }

}
