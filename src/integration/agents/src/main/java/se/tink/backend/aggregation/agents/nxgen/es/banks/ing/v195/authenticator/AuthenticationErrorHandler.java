package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc.ErrorCodeMessage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class AuthenticationErrorHandler {

    public static void handlePostSessionErrors(HttpResponseException hre)
            throws LoginException, AuthorizationException {
        ErrorResponse errorResponse = hre.getResponse().getBody(ErrorResponse.class);
        if (hre.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
            if (errorResponse.hasErrorField(ErrorCodes.LOGIN_DOCUMENT_FIELD)) {
                log.warn("Login document didn't pass server-side validation");
                throw LoginError.INCORRECT_CREDENTIALS.exception(hre);
            } else if (errorResponse.hasErrorField(ErrorCodes.BIRTHDAY_FIELD)) {
                log.warn("Birthday date didn't pass server-side validation");
                throw LoginError.INCORRECT_CREDENTIALS.exception(hre);
            }
        } else if (hre.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN
                && errorResponse.hasErrorCode(ErrorCodes.GENERIC_LOCK)) {
            log.warn("Account blocked");
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(hre);
        }
    }

    public static void handlePutSessionErrors(HttpResponseException hre) throws LoginException {
        ErrorCodeMessage error = hre.getResponse().getBody(ErrorCodeMessage.class);
        if (hre.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
            if (error.getErrorCode() == ErrorCodes.ACCOUNT_BLOCKED) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        "Account seems to be blocked. Message from the bank: "
                                + error.getMessage());
            } else if (error.getErrorCode() == ErrorCodes.INVALID_PIN) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(
                        "Supplied otp or pinpad code seems to be invalid. Message from the bank: "
                                + error.getMessage());
            }
            throw LoginError.INCORRECT_CREDENTIALS.exception(hre);
        } else if (hre.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(hre);
        }
    }
}
