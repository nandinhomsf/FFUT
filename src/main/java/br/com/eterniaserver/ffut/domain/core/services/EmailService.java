package br.com.eterniaserver.ffut.domain.core.services;

import br.com.eterniaserver.ffut.Constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class EmailService {

    private final RestClient restClient;

    public EmailService(RestClient.Builder restClientBuilder,
                        @Value("${resend.api-url:https://api.resend.com}") String apiUrl) {
        this.restClient = restClientBuilder.baseUrl(apiUrl).build();
    }

    @Value("${resend.from}")
    private String fromEmailAddress;

    @Value("${resend.enabled:false}")
    private Boolean enabled;

    @Value("${resend.api-key:}")
    private String apiKey;

    @Async
    public void sendEmail(String recipient, String subject, String content) throws ResponseStatusException {
        if (!enabled) {
            return;
        }

        if (apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Constants.EMAIL_SERVICE_ERROR);
        }

        try {
            restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(Map.of(
                            "from", fromEmailAddress,
                            "to", recipient,
                            "subject", subject,
                            "html", content
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Constants.EMAIL_SERVICE_ERROR);
        }
    }

}
