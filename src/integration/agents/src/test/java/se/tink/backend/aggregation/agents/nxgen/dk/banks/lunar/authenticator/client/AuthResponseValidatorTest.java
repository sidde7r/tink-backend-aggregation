package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import agents_platform_framework.org.springframework.http.HttpStatus;
import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class AuthResponseValidatorTest {

    private AuthResponseValidator authResponseValidator;

    @Test
    @Parameters(method = "errorResponses")
    public void shouldThrowExceptionForInvalidResponse(
            ResponseEntity<String> responseEntity, int status, String reason) {
        // given
        authResponseValidator = new AuthResponseValidator();

        // when
        ResponseStatusException throwable =
                (ResponseStatusException)
                        catchThrowable(() -> authResponseValidator.validate(responseEntity));

        // then
        assertThat(throwable.getStatus()).isEqualTo(HttpStatus.valueOf(status));
        assertThat(throwable.getReason()).isEqualTo(reason);
    }

    private Object[] errorResponses() {
        return new Object[] {
            new Object[] {ResponseEntity.status(400).body("test"), 400, "test"},
            new Object[] {ResponseEntity.status(400).build(), 400, ""},
            new Object[] {ResponseEntity.status(401).build(), 401, ""},
            new Object[] {
                ResponseEntity.status(403).body("{\"some\": \"test\"}"), 403, "{\"some\": \"test\"}"
            },
            new Object[] {ResponseEntity.status(403).build(), 403, ""},
            new Object[] {ResponseEntity.status(500).build(), 500, ""},
            new Object[] {ResponseEntity.status(501).body("hello"), 501, "hello"},
        };
    }

    @Test
    @Parameters(method = "validResponses")
    public void shouldNotThrowExceptionWhenValidResponse(ResponseEntity<String> responseEntity) {
        // given
        authResponseValidator = new AuthResponseValidator();

        // when & then
        assertThatCode(() -> authResponseValidator.validate(responseEntity))
                .doesNotThrowAnyException();
    }

    private Object[] validResponses() {
        return new Object[] {
            new Object[] {ResponseEntity.ok("")},
            new Object[] {ResponseEntity.created(URI.create("location")).body("testo")},
            new Object[] {ResponseEntity.accepted().body("hello")},
            new Object[] {ResponseEntity.status(300).body("multiple choices pal")},
            new Object[] {ResponseEntity.status(301).body("moved permanently bro")},
        };
    }
}
