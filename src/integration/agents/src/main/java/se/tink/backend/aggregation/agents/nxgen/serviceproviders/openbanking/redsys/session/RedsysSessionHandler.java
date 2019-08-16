package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.session;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentStorage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public final class RedsysSessionHandler implements SessionHandler {

    private final RedsysApiClient apiClient;
    private final RedsysConsentStorage consentStorage;

    public RedsysSessionHandler(RedsysApiClient apiClient, RedsysConsentStorage consentStorage) {
        this.apiClient = apiClient;
        this.consentStorage = consentStorage;
    }

    @Override
    public void logout() {
        // TODO: cancel and clear consent?
    }

    @Override
    public void keepAlive() throws SessionException {
        if (apiClient.hasValidAccessToken()) {
            final String consentId = consentStorage.getConsentId();
            if (Strings.isNullOrEmpty(consentId)) {
                // No consent was ever created
                throw SessionError.SESSION_EXPIRED.exception();
            }
            try {
                // Session is alive even if consent is not valid
                // A new consent will be requested when needed
                apiClient.fetchConsentStatus(consentId);
                return;
            } catch (Exception e) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
