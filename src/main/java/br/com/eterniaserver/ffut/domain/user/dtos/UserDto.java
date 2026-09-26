package br.com.eterniaserver.ffut.domain.user.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record UserDto(String login,
                      String userId,
                      String username,
                      String name,
                      String surname,
                      List<String> roles,
                      LocalDateTime createdAt) { }
