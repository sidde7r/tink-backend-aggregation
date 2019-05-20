package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.session;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class RedsysSessionHandler implements SessionHandler {

    private final RedsysApiClient apiClient;
    private final SessionStorage sessionStorage;

    public RedsysSessionHandler(RedsysApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        sessionStorage.clear();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (apiClient.hasValidAccessToken()) {
            final String consentId = sessionStorage.get(RedsysConstants.StorageKeys.CONSENT_ID);
            if (Strings.isNullOrEmpty(consentId)) {
                // no consent
                throw SessionError.SESSION_EXPIRED.exception();
            }
            try {
                final String consentStatus = apiClient.fetchConsentStatus(consentId);
                if (consentStatus.equalsIgnoreCase(RedsysConstants.ConsentStatus.VALID)) {
                    return;
                }
            } catch (Exception e) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
