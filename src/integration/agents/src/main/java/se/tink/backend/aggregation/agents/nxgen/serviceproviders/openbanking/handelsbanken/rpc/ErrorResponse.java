package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Errors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    @JsonProperty private String error;

    public void handleErrors() throws LoginException, BankIdException {
        if (Errors.INVALID_REQUEST.equalsIgnoreCase(error)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        if (Errors.NOT_SHB_APPROVED.equalsIgnoreCase(error)) {
            throw BankIdError.BANK_ID_UNAUTHORIZED_ISSUER.exception();
        }
    }
}
