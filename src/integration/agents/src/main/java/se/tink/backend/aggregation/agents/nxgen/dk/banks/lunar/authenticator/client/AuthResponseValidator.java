package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client;

import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import agents_platform_framework.org.springframework.http.HttpStatus;
import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import java.util.Objects;

public class AuthResponseValidator {

    <T> void validate(ResponseEntity<T> responseEntity) {
        if (responseEntity.getStatusCode().isError()) {
            throw new ResponseStatusException(
                    HttpStatus.valueOf(responseEntity.getStatusCode().value()),
                    Objects.toString(responseEntity.getBody(), ""));
        }
    }
}
