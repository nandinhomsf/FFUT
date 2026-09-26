package br.com.eterniaserver.ffut.domain.user.models;

import br.com.eterniaserver.ffut.domain.user.dtos.UserDto;

import jakarta.validation.constraints.NotNull;

public record ReadUserResponse(@NotNull UserDto userDto) { }
