package se.tink.agent.sdk.authentication.authenticators.oauth2;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.user_interaction.UserResponseData;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public final class Oauth2Utils {
    private Oauth2Utils() {}

    public static void handleCallbackDataError(UserResponseData callbackData) {
        Optional<String> error = callbackData.tryGet(Oauth2Constants.CallbackParams.ERROR);
        if (!error.isPresent()) {
            log.debug("[OAuth2] callback success.");
            return;
        }

        Optional<String> errorDescription =
                callbackData.tryGet(Oauth2Constants.CallbackParams.ERROR_DESCRIPTION);

        Oauth2Constants.ErrorType errorType = Oauth2Constants.ErrorType.getErrorType(error.get());

        if (Oauth2Constants.ErrorType.ACCESS_DENIED.equals(errorType)
                || Oauth2Constants.ErrorType.LOGIN_REQUIRED.equals(errorType)) {
            log.info(
                    "[OAuth2] {} callback: {}",
                    errorType.getValue(),
                    SerializationUtils.serializeToString(callbackData));
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (Oauth2Constants.ErrorType.CANCELED_BY_USER.equals(errorType)
                || Oauth2Constants.ErrorType.USER_CANCELED_AUTHORIZATION.equals(errorType)
                || Oauth2Constants.ErrorType.INVALID_AUTHENTICATION.equals(errorType)) {
            log.info("[OAuth2] cancelled by user");
            throw ThirdPartyAppError.CANCELLED.exception();
        } else if (Oauth2Constants.ErrorType.SERVER_ERROR.equals(errorType)
                || Oauth2Constants.ErrorType.TEMPORARILY_UNAVAILABLE.equals(errorType)) {
            log.info(
                    "[OAuth2] {}: error_description: {}",
                    errorType.getValue(),
                    errorDescription.orElse(""));
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    errorDescription.orElse("no error description"));
        }

        throw new IllegalStateException(
                String.format(
                        "OAuth2 unknown error: error=%s, error_description=%s.",
                        error.orElse(""), errorDescription.orElse("")));
    }
}
