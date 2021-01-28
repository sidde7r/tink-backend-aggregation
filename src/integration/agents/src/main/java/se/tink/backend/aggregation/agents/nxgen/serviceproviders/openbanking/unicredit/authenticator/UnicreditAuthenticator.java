package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator;

import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import java.text.ParseException;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class UnicreditAuthenticator {

    private final UnicreditPersistentStorage unicreditStorage;
    private final UnicreditBaseApiClient apiClient;
    private final Credentials credentials;

    void autoAuthenticate() throws SessionException {
        unicreditStorage.getConsentId().orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (isSavedConsentInvalid()) {
            clearConsent();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    URL buildAuthorizeUrl(String state) {
        ConsentResponse consentResponse = apiClient.createConsent(state);
        unicreditStorage.saveConsentId(consentResponse.getConsentId());
        return apiClient.getScaRedirectUrlFromConsentResponse(consentResponse);
    }

    boolean isSavedConsentInvalid() {
        try {
            return !apiClient.getConsentStatus().isValidConsent();

        } catch (HttpResponseException hre) {
            if (isInvalidConsentException(hre)) {
                return true;
            }
            throw hre;
        }
    }

    void clearConsent() {
        unicreditStorage.removeConsentId();
    }

    void setSessionExpiryDateBasedOnConsent() throws SessionException, ThirdPartyAppException {
        ConsentDetailsResponse consentDetails = apiClient.getConsentDetails();
        try {
            credentials.setSessionExpiryDate(FORMATTER_DAILY.parse(consentDetails.getValidUntil()));
        } catch (ParseException e) {
            throw new ThirdPartyAppException(ThirdPartyAppError.AUTHENTICATION_ERROR);
        }
    }

    private boolean isInvalidConsentException(HttpResponseException httpResponseException) {
        final String message = httpResponseException.getResponse().getBody(String.class);
        return message.contains(ErrorCodes.CONSENT_INVALID.name())
                || message.contains(ErrorCodes.CONSENT_EXPIRED.name())
                || message.contains(ErrorCodes.CONSENT_UNKNOWN.name());
    }
}
