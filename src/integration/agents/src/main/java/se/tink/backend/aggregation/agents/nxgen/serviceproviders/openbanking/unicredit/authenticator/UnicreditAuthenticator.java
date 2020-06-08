package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator;

import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import java.text.ParseException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UnicreditAuthenticator {

    private final UnicreditBaseApiClient apiClient;
    private final Credentials credentials;

    public UnicreditAuthenticator(UnicreditBaseApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    public boolean isConsentValid() throws SessionException {
        return apiClient.getConsentStatus().isValidConsent();
    }

    public void autoAuthenticate() throws SessionException {
        ConsentStatusResponse consentStatus;
        try {
            consentStatus = apiClient.getConsentStatus();
        } catch (HttpResponseException e) {
            handleInvalidConsents(e);
            return;
        }

        if (!consentStatus.isValidConsent()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private void handleInvalidConsents(HttpResponseException httpResponseException)
            throws SessionException {
        final String message = httpResponseException.getResponse().getBody(String.class);
        if (isConsentsProblem(message)) {
            apiClient.removeConsentFromPersistentStorage();
            throw SessionError.SESSION_EXPIRED.exception();
        }

        throw httpResponseException;
    }

    private boolean isConsentsProblem(String message) {
        return message.contains(ErrorCodes.CONSENT_INVALID.name())
                || message.contains(ErrorCodes.CONSENT_EXPIRED.name())
                || message.contains(ErrorCodes.CONSENT_UNKNOWN.name());
    }

    void setSessionExpiryDateBasedOnConsent() throws SessionException, ThirdPartyAppException {
        ConsentDetailsResponse consentDetails = apiClient.getConsentDetails();
        try {
            credentials.setSessionExpiryDate(FORMATTER_DAILY.parse(consentDetails.getValidUntil()));
        } catch (ParseException e) {
            throw new ThirdPartyAppException(ThirdPartyAppError.AUTHENTICATION_ERROR);
        }
    }
}
