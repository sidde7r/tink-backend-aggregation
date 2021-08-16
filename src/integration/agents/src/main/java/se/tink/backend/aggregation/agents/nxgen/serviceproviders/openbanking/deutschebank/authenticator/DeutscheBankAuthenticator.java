package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.utils.berlingroup.BerlingroupConstants.StatusValues;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class DeutscheBankAuthenticator {

    private static final String SCA_STATUS_URL = "sca_status_url";

    private final DeutscheBankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;

    public URL authenticate(String state) {
        ConsentResponse consent =
                apiClient.getConsent(state, credentials.getField(CredentialKeys.USERNAME));
        persistentStorage.put(StorageKeys.CONSENT_ID, consent.getConsentId());
        sessionStorage.put(SCA_STATUS_URL, consent.getLinks().getScaStatus());
        return new URL(consent.getLinks().getScaRedirect());
    }

    public void verifyPersistedConsentIdIsValid() {
        if (!isPersistedConsentIdValid()) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
        }
    }

    public void verifyPersistedConsentIdIsNotExpired() {
        if (!isPersistedConsentIdValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    public void storeSessionExpiry() {
        ConsentDetailsResponse consentDetailsResponse = apiClient.getConsentDetails();
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    public boolean isPersistedConsentIdValid() {
        ConsentResponse consentResponse = apiClient.getConsentResponse();

        if (consentResponse == null) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
        }

        if (StatusValues.REJECTED.equalsIgnoreCase(consentResponse.getConsentStatus())) {
            throw ThirdPartyAppError.CANCELLED.exception();
        }
        return StatusValues.VALID.equalsIgnoreCase(consentResponse.getConsentStatus());
    }

    public AuthorizationStatusResponse getAuthorisationDetails() {
        return apiClient.getAuthorisationDetails(sessionStorage.get(SCA_STATUS_URL));
    }
}
