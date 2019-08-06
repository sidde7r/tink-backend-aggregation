package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class UnicreditAuthenticator {

    private final UnicreditBaseApiClient apiClient;

    public UnicreditAuthenticator(UnicreditBaseApiClient apiClient) {
        this.apiClient = apiClient;
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
}
