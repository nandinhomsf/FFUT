package br.com.eterniaserver.ffut.domain.user.rest;

import br.com.eterniaserver.ffut.domain.user.models.*;
import br.com.eterniaserver.ffut.domain.user.services.UserAccountService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/user/")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    public UserDetails getCurrentUser() {
        Authentication authenticationToken = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetails) authenticationToken.getPrincipal();
    }

    @GetMapping("rank/list/")
    @ResponseStatus(HttpStatus.OK)
    public ListUserRankResponse rankList(@RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) {
        return userAccountService.rankList(page.orElse(0), size.orElse(10));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse create(@RequestBody @Valid CreateUserRequest request) {
        return userAccountService.create(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ReadUserResponse read() {
        UserDetails userDetails = getCurrentUser();
        return userAccountService.read(new ReadUserRequest(userDetails.getUsername()));
    }

    @GetMapping("{login}/")
    @ResponseStatus(HttpStatus.OK)
    public ReadUserResponse read(@PathVariable String login) {
        return userAccountService.read(new ReadUserRequest(login));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UpdateUserResponse update(@RequestBody @Valid UpdateUserRequest request) {
        UserDetails userDetails = getCurrentUser();

        request.setUserDetails(userDetails);

        return userAccountService.update(request);
    }

    @DeleteMapping("{login}/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public DeleteUserResponse delete(@PathVariable String login) {
        return userAccountService.delete(new DeleteUserRequest(login));
    }

}
