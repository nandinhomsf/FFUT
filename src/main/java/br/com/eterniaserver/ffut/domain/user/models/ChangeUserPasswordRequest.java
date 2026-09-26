package br.com.eterniaserver.ffut.domain.user.models;

public record ChangeUserPasswordRequest(String password, String token) {}
