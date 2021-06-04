package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.ErrorType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class OAuth2AuthenticationFlow {
    public static Authenticator create(
            CredentialsRequest request,
            SystemUpdater systemUpdater,
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth2Authenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState) {

        OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
    }

    private static Optional<String> getCallbackElement(
            Map<String, String> callbackData, String key) {
        String value = callbackData.getOrDefault(key, null);
        if (Strings.isNullOrEmpty(value)) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    public static void handleErrors(Map<String, String> callbackData)
            throws AuthenticationException {
        Optional<String> error = getCallbackElement(callbackData, CallbackParams.ERROR);
        Optional<String> errorDescription =
                getCallbackElement(callbackData, OAuth2Constants.CallbackParams.ERROR_DESCRIPTION);

        if (!error.isPresent()) {
            log.info("[OAuth2] callback success.");
            return;
        }

        ErrorType errorType = ErrorType.getErrorType(error.get());
        if (ErrorType.ACCESS_DENIED.equals(errorType)
                || ErrorType.LOGIN_REQUIRED.equals(errorType)) {
            log.info(
                    "[OAuth2] {} callback: {}",
                    errorType.getValue(),
                    SerializationUtils.serializeToString(callbackData));
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (ErrorType.CANCELED_BY_USER.equals(errorType)) {
            log.info("[OAuth2] cancelled by user");
            throw ThirdPartyAppError.CANCELLED.exception();
        } else if (ErrorType.SERVER_ERROR.equals(errorType)
                || ErrorType.TEMPORARILY_UNAVAILABLE.equals(errorType)) {
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
