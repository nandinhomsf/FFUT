package br.com.eterniaserver.ffut.domain.user.models;

import br.com.eterniaserver.ffut.domain.user.dtos.TokenDto;

import jakarta.validation.constraints.NotNull;

public record AuthenticateResponse(@NotNull TokenDto tokenDto) { }
