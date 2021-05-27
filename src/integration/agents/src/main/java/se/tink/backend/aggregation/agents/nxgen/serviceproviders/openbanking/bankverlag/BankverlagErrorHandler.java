package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.PsuErrorMessages;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.TppMessage;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class BankverlagErrorHandler {

    private static final String FORMAT_ERROR = "FORMAT_ERROR";

    private static final TppMessage PSU_CREDENTIALS_INVALID =
            TppMessage.builder().category(TppMessage.ERROR).code("PSU_CREDENTIALS_INVALID").build();

    private static final TppMessage OTP_WRONG_FORMAT =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code(FORMAT_ERROR)
                    .text(
                            "Format of certain request fields are not matching the XS2A requirements.")
                    .build();
    private static final TppMessage OTP_WRONG_LENGTH =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code(FORMAT_ERROR)
                    .text("scaAuthenticationData muss auf Ausdruck \"[0-9]{6}\" passen")
                    .build();

    private static final TppMessage PSU_TOO_LONG =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code(FORMAT_ERROR)
                    .text("PSU-ID zu lang.")
                    .build();

    private static final TppMessage NO_SCA_METHOD =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code("SCA_INVALID")
                    .text("No active/ usable scaMethods defined for PSU.")
                    .build();

    enum ErrorSource {
        AUTHORISATION_USERNAME_PASSWORD,
        AUTHORISATION_OTP,
        AUTHORISATION_SELECT_METHOD
    }

    static void handleError(HttpResponseException httpResponseException, ErrorSource errorSource) {
        Optional<ErrorResponse> maybeErrorResponse =
                ErrorResponse.fromHttpException(httpResponseException);

        if (maybeErrorResponse.isPresent()) {
            ErrorResponse errorResponse = maybeErrorResponse.get();
            AgentError error;
            switch (errorSource) {
                case AUTHORISATION_OTP:
                    error = handleAuthorisationOtpErrors(errorResponse);
                    break;
                case AUTHORISATION_USERNAME_PASSWORD:
                    error = handleUsernamePasswordErrors(errorResponse);
                    break;
                case AUTHORISATION_SELECT_METHOD:
                    error = handleSelectMethodErrors(errorResponse);
                    break;
                default:
                    return;
            }
            if (error != null) {
                throw error.exception(httpResponseException);
            }
        }
    }

    private static AgentError handleAuthorisationOtpErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .or(ErrorResponse.anyTppMessageMatchesPredicate(OTP_WRONG_FORMAT))
                .or(ErrorResponse.anyTppMessageMatchesPredicate(OTP_WRONG_LENGTH))
                .test(errorResponse)) {
            return LoginError.INCORRECT_CHALLENGE_RESPONSE;
        }
        return null;
    }

    private static AgentError handleUsernamePasswordErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.TEMPORARILY_BLOCKED_ACCOUNT)
                .or(ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.BLOCKED_ACCOUNT))
                .test(errorResponse)) {
            return AuthorizationError.ACCOUNT_BLOCKED;
        }
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .or(ErrorResponse.anyTppMessageMatchesPredicate(PSU_TOO_LONG))
                .test(errorResponse)) {
            return LoginError.INCORRECT_CREDENTIALS;
        }
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.NO_ACTIVE_TAN_MEDIUM)
                .test(errorResponse)) {
            return LoginError.NO_AVAILABLE_SCA_METHODS;
        }
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.PLEASE_CHANGE_PIN)
                .test(errorResponse)) {
            return LoginError.PASSWORD_CHANGE_REQUIRED;
        }
        if (ErrorResponse.anyTppMessageMatchesPredicate(NO_SCA_METHOD).test(errorResponse)) {
            return LoginError.NO_AVAILABLE_SCA_METHODS;
        }
        return null;
    }

    private static AgentError handleSelectMethodErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(NO_SCA_METHOD).test(errorResponse)) {
            // NZG-486 This in theory shouldn't happen, but it does. Extra log line to gather such
            // cases and ask bank about it
            log.info("Error - no usable SCA - during sca method selection occurred!");
            return LoginError.NO_AVAILABLE_SCA_METHODS;
        }
        return null;
    }
}
