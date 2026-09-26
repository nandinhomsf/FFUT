package br.com.eterniaserver.ffut.domain.user.models;

import br.com.eterniaserver.ffut.Constants;
import jakarta.validation.constraints.NotNull;

public record VerifyTokenRequest(@NotNull(message = Constants.TOKEN_NEEDED) String token) { }
