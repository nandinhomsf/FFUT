package br.com.eterniaserver.ffut.domain.user.services;

import br.com.eterniaserver.ffut.Constants;
import br.com.eterniaserver.ffut.domain.user.dtos.UserDto;
import br.com.eterniaserver.ffut.domain.user.entities.UserAccountEntity;
import br.com.eterniaserver.ffut.domain.user.enums.BaseRoles;
import br.com.eterniaserver.ffut.domain.user.models.*;
import br.com.eterniaserver.ffut.domain.user.repositories.UserAccountRepository;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class UserAccountService {

    private final PasswordEncoder encoder;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public ListUserRankResponse rankList(int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("score"));
        Pageable pageRequest = PageRequest.of(page, size, sort);

        List<UserAccountEntity> result = userAccountRepository.findAll(pageRequest).toList();

        return new ListUserRankResponse(
                IntStream.range(0, result.size())
                        .mapToObj(i -> result.get(i).toRank(i))
                        .toList(),
                userAccountRepository.count()
        );
    }

    @Transactional
    public CreateUserResponse create(CreateUserRequest request) {
        if (userAccountRepository.existsByLogin(request.login())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, Constants.LOGIN_IN_USE);
        }

        UserAccountEntity userAccountEntity = new UserAccountEntity();

        userAccountEntity.setLogin(request.login());
        userAccountEntity.setPassword(encoder.encode(request.password()));
        userAccountEntity.setName(request.name());
        userAccountEntity.setSurname(request.surname());
        userAccountEntity.setLocale(request.locale());
        userAccountEntity.addRole(BaseRoles.USER);
        userAccountEntity.setCreatedAt(LocalDateTime.now());

        userAccountEntity.validate();

        userAccountEntity = userAccountRepository.save(userAccountEntity);

        UserDto userDto = userAccountEntity.toDto();

        return new CreateUserResponse(userDto);
    }

    @Transactional(readOnly = true)
    public ReadUserResponse read(ReadUserRequest request) {
        UserAccountEntity userAccountEntity = userAccountRepository
                .findByLogin(request.login())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Constants.USER_NOT_FOUND));

        UserDto userDto = userAccountEntity.toDto();

        return new ReadUserResponse(userDto);
    }

    @Transactional
    public UpdateUserResponse update(UpdateUserRequest request) {
        boolean sameLogin = request.getLogin().equals(request.getUserDetails().getUsername());
        if (!sameLogin && userAccountRepository.existsByLogin(request.getLogin())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, Constants.LOGIN_IN_USE);
        }

        UserAccountEntity userAccountEntity = userAccountRepository
                .findByLogin(request.getUserDetails().getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Constants.USER_NOT_FOUND));

        userAccountEntity.setLogin(request.getLogin());
        userAccountEntity.setName(request.getName());
        userAccountEntity.setSurname(request.getSurname());

        userAccountEntity.validate();

        userAccountEntity = userAccountRepository.save(userAccountEntity);

        UserDto userDto = userAccountEntity.toDto();

        return new UpdateUserResponse(userDto);
    }

    @Transactional
    public DeleteUserResponse delete(DeleteUserRequest request) {
        if (!userAccountRepository.existsByLogin(request.login())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Constants.USER_NOT_FOUND);
        }

        userAccountRepository.deleteByLogin(request.login());

        return new DeleteUserResponse();
    }

    @Transactional(readOnly = true)
    public UserDetails login(String login) {
        UserAccountEntity userAccountEntity = userAccountRepository
                .findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Constants.USER_NOT_FOUND));

        return User
                .builder()
                .username(userAccountEntity.getLogin())
                .password(userAccountEntity.getPassword())
                .roles(userAccountEntity.getRoles().toArray(String[]::new))
                .disabled(false)
                .build();
    }

}
