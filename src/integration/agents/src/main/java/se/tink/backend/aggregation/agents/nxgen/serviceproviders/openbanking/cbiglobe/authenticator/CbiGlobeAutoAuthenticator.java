package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.CbiConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;

@RequiredArgsConstructor
public class CbiGlobeAutoAuthenticator implements AutoAuthenticator {

    private final CbiGlobeAuthApiClient authApiClient;
    private final CbiStorage storage;

    @Override
    public void autoAuthenticate() {
        String consentId = storage.getConsentId();
        if (consentId == null) {
            throw SessionError.CONSENT_INVALID.exception("[CBI] No consent in storage");
        }

        CbiConsentStatus consentStatus =
                authApiClient.fetchConsentStatus(consentId).getConsentStatus();

        if (consentStatus.isExpired() || consentStatus.isPendingExpired()) {
            throw SessionError.CONSENT_EXPIRED.exception();
        }

        if (consentStatus.isReceived()
                || consentStatus.isRejected()
                || consentStatus.isTerminatedByTpp()
                || consentStatus.isPartiallyAuthorised()) {
            throw SessionError.CONSENT_INVALID.exception();
        }

        if (consentStatus.isRevokedByPsu()) {
            throw SessionError.CONSENT_REVOKED_BY_USER.exception();
        }

        if (consentStatus.isInvalidated() || consentStatus.isReplaced()) {
            throw SessionError.CONSENT_REVOKED.exception();
        }
    }
}
