package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class JyskeKeyCardExceptionHandler {

    private static final ErrorResponse WRONG_CODE = new ErrorResponse(1, null, null, "BAD_REQUEST");

    private final ErrorResponse errorResponse;

    JyskeKeyCardExceptionHandler(HttpResponseException httpResponseException) {
        errorResponse = httpResponseException.getResponse().getBody(ErrorResponse.class);
    }

    void handle() {
        log.info("KeyCard nemIdEnroll: {}", errorResponse.toString());

        if (WRONG_CODE.equals(errorResponse)) {
            throw SupplementalInfoError.NO_VALID_CODE.exception();
        } else {
            throw SupplementalInfoError.UNKNOWN.exception();
        }
    }
}
