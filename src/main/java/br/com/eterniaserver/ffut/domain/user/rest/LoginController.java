package br.com.eterniaserver.ffut.domain.user.rest;

import br.com.eterniaserver.ffut.Constants;
import br.com.eterniaserver.ffut.domain.user.entities.UserAccountEntity;
import br.com.eterniaserver.ffut.domain.user.models.*;
import br.com.eterniaserver.ffut.domain.user.repositories.UserAccountRepository;
import br.com.eterniaserver.ffut.domain.user.services.JWTService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@RestController
@RequestMapping("/login/")
@RequiredArgsConstructor
public class LoginController {

    private final JWTService jwtService;
    private final UserAccountRepository userAccountRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("verify/{token}/")
    @ResponseStatus(HttpStatus.PERMANENT_REDIRECT)
    public RedirectView verify(@PathVariable String token) {
        if (!jwtService.isValidToken(token)) {
            return new RedirectView(frontendUrl + "/email-confirm?error=invalid_token");
        }

        UserAccountEntity userAccountEntity = userAccountRepository
                .findByLogin(jwtService.getUserLogin(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Constants.USER_NOT_FOUND));

        userAccountEntity.setVerified(true);

        userAccountRepository.save(userAccountEntity);

        AuthenticateResponse response = jwtService.authenticate(userAccountEntity);

        return new RedirectView(frontendUrl + "/email-confirm?" + response.tokenDto().toQueryParameters());
    }

    @PostMapping("change-password/")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(@RequestBody @Valid ChangeUserPasswordRequest request) {
        UserAccountEntity userAccountEntity = userAccountRepository
                .findByLogin(jwtService.getUserLogin(request.token()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Constants.USER_NOT_FOUND));

        jwtService.changePassword(userAccountEntity, request.password());
    }

    @PostMapping("recovery/{email}/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recoveryPassword(@PathVariable String email) {
        Optional<UserAccountEntity> accountOptional = userAccountRepository.findByLogin(email);

        accountOptional.ifPresent(jwtService::sendRecoveryEmail);
    }

    @PostMapping("check/")
    @ResponseStatus(HttpStatus.OK)
    public VerifyTokenResponse verify(@RequestBody @Valid VerifyTokenRequest request) {
        return jwtService.verify(request);
    }

    @PostMapping("auth/")
    @ResponseStatus(HttpStatus.OK)
    public AuthenticateResponse authenticate(@RequestBody @Valid AuthenticateRequest request) {
        UserAccountEntity userAccountEntity = userAccountRepository
                .findByLogin(request.getLogin())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Constants.USER_NOT_FOUND));

        request.setUserAccountEntity(userAccountEntity);

        return jwtService.authenticate(request);
    }

}
