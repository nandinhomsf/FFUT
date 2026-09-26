package br.com.eterniaserver.ffut.domain.user.services;

import br.com.eterniaserver.ffut.Constants;
import br.com.eterniaserver.ffut.domain.core.services.EmailService;
import br.com.eterniaserver.ffut.domain.user.dtos.TokenDto;
import br.com.eterniaserver.ffut.domain.user.entities.UserAccountEntity;

import br.com.eterniaserver.ffut.domain.user.models.AuthenticateRequest;
import br.com.eterniaserver.ffut.domain.user.models.AuthenticateResponse;
import br.com.eterniaserver.ffut.domain.user.models.VerifyTokenRequest;
import br.com.eterniaserver.ffut.domain.user.models.VerifyTokenResponse;

import br.com.eterniaserver.ffut.domain.user.repositories.UserAccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class JWTService {

    private final Logger LOGGER = Logger.getGlobal();
    private final UserAccountRepository userAccountRepository;

    @Value("${spring.security.jwt.expiration}")
    private String expiration;

    @Value("${spring.security.jwt.signature-key}")
    private String signatureKey;

    @Value("${application.domain}")
    private String applicationDomain;

    @Value("${frontend.domain}")
    private String frontendDomain;

    private Optional<SecretKey> secretKey = Optional.empty();

    private final SpringTemplateEngine springTemplateEngine;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public JWTService(SpringTemplateEngine springTemplateEngine,
                      PasswordEncoder passwordEncoder,
                      EmailService emailService, UserAccountRepository userAccountRepository) {
        this.springTemplateEngine = springTemplateEngine;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userAccountRepository = userAccountRepository;
    }

    public VerifyTokenResponse verify(VerifyTokenRequest request) {
        if (!request.token().contains("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.INVALID_TOKEN);
        }

        String token = request.token().split(" ")[1];
        if (!isValidToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, Constants.INVALID_TOKEN);
        }

        return new VerifyTokenResponse(true);
    }

    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        UserAccountEntity userAccountEntity = request.getUserAccountEntity();
        String password = request.getPassword();

        if (!passwordEncoder.matches(password, userAccountEntity.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, Constants.INVALID_CREDENTIALS);
        }

        return this.authenticate(userAccountEntity);
    }

    public AuthenticateResponse authenticate(UserAccountEntity userAccount) {
        long exp = userAccount.getVerified() ? Long.parseLong(expiration) : 5L;
        LocalDateTime expDate = LocalDateTime.now().plusMinutes(exp);
        Date date = Date.from(expDate.atZone(ZoneId.systemDefault()).toInstant());

        String login = userAccount.getLogin();
        String token = Jwts.builder().subject(login).expiration(date).signWith(getSecretKey()).compact();
        String[] roles = userAccount.getRoles().toArray(String[]::new);

        if (!userAccount.getVerified()) {
            sendVerificationEmail(userAccount, token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, Constants.INVALID_NOT_VERIFIED);
        }

        return new AuthenticateResponse(new TokenDto(login, token, roles, date));
    }

    public String getUserLogin(String token) {
        return decodeToken(token).getSubject();
    }

    public boolean isValidToken(String token) {
        try {
            Claims claims = decodeToken(token);

            Date expDate = claims.getExpiration();
            LocalDateTime date = expDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            return !LocalDateTime.now().isAfter(date);
        } catch (Exception exception) {
            LOGGER.log(Level.INFO, exception.getMessage());
            return false;
        }
    }

    public void sendRecoveryEmail(UserAccountEntity userAccountEntity) {
        LocalDateTime expDate = LocalDateTime.now().plusMinutes(5L);
        Date date = Date.from(expDate.atZone(ZoneId.systemDefault()).toInstant());

        String token = Jwts
                .builder()
                .subject(userAccountEntity.getLogin())
                .expiration(date)
                .signWith(getSecretKey())
                .compact();

        Context thymeleafContext = new Context();
        Map<String, Object> variables = new HashMap<>();

        String urlLink = frontendDomain + "/login/recovery?token=" + token;
        String templateName = "recovery-account."  + userAccountEntity.getLocale() + ".html";

        variables.put("link", urlLink);

        thymeleafContext.setVariables(variables);

        String htmlBody = springTemplateEngine.process(templateName, thymeleafContext);

        emailService.sendEmail(userAccountEntity.getLogin(), "Login - Recovery", htmlBody);
    }

    public void changePassword(UserAccountEntity userAccountEntity, String password) {
        userAccountEntity.setPassword(passwordEncoder.encode(password));

        userAccountRepository.save(userAccountEntity);
    }

    private void sendVerificationEmail(UserAccountEntity userAccountEntity, String token) throws ResponseStatusException {
        Context thymeleafContext = new Context();
        Map<String, Object> variables = new HashMap<>();

        String urlLink = applicationDomain + "/login/verify/" + token + "/";
        String templateName = "verify-account."  + userAccountEntity.getLocale() + ".html";

        variables.put("link", urlLink);

        thymeleafContext.setVariables(variables);

        String htmlBody = springTemplateEngine.process(templateName, thymeleafContext);

        emailService.sendEmail(userAccountEntity.getLogin(), "Login - Check", htmlBody);
    }

    private Claims decodeToken(String token) {
        return Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKey() {
        if (secretKey.isEmpty())
            secretKey = Optional.of(Keys.hmacShaKeyFor(signatureKey.getBytes(StandardCharsets.UTF_8)));

        return secretKey.get();
    }

}
