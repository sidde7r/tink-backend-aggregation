package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.MapExtractor.getCallbackElement;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.ErrorDescriptions.SERVER_ERROR_PROCESSING;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Errors.ACCESS_DENIED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Errors.INVALID_INTENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Errors.LOGIN_REQUIRED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Errors.SERVER_ERROR;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Errors.TEMPORARILY_UNAVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Errors.UNAUTHORISED;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.data.ConsentDataStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
@Slf4j
final class OpenIdAuthenticationErrorHandler implements ErrorHandler {

    private final ConsentDataStorage consentDataStorage;
    private final OpenIdApiClient openIdApiClient;

    @Override
    public void handle(Map<String, String> callbackData) {
        Optional<String> error =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ERROR);

        if (!error.isPresent()) {
            log.info("[OpenIdAuthenticationErrorHandler] OpenId callback success.");
            return;
        }

        String serializedCallbackData = SerializationUtils.serializeToString(callbackData);
        String consentId = consentDataStorage.restoreConsentId();
        log.info(
                "[OpenIdAuthenticationErrorHandler] OpenId callback data: {} for consentId {}",
                serializedCallbackData,
                consentId);

        String errorType = error.orElse("");
        String errorDescription =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ERROR_DESCRIPTION)
                        .orElse("");

        if (errorType.equalsIgnoreCase(ACCESS_DENIED)
                || errorType.equalsIgnoreCase(LOGIN_REQUIRED)) {
            // Store error information to make it possible for agent to determine cause and
            // give end user a proper error message.
            openIdApiClient.storeOpenIdError(ErrorEntity.create(errorType, errorDescription));
            throw LoginError.INCORRECT_CREDENTIALS.exception();

        } else if (errorType.equalsIgnoreCase(SERVER_ERROR)) {
            if (errorDescription.equalsIgnoreCase(SERVER_ERROR_PROCESSING)) {
                throw ThirdPartyAppError.CANCELLED.exception(errorDescription);
            }
            throw BankServiceError.BANK_SIDE_FAILURE.exception(errorDescription);

        } else if (errorType.equalsIgnoreCase(TEMPORARILY_UNAVAILABLE)) {
            throw BankServiceError.NO_BANK_SERVICE.exception(errorDescription);

        } else if (errorType.equalsIgnoreCase(UNAUTHORISED)) {
            throw BankServiceError.SESSION_TERMINATED.exception(errorDescription);

        } else if (errorType.equalsIgnoreCase(INVALID_INTENT_ID)) {
            throw SessionError.CONSENT_INVALID.exception();
        }

        throw new IllegalStateException(
                String.format(
                        "[OpenIdAuthenticationErrorHandler] Unknown error with details: "
                                + "{errorType: %s, errorDescription: %s}",
                        errorType, errorDescription));
    }
}
