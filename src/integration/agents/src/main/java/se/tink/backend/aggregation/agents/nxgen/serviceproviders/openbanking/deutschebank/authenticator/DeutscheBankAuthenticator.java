package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DeutscheBankAuthenticator {

    private final DeutscheBankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    public DeutscheBankAuthenticator(
            DeutscheBankApiClient apiClient,
            PersistentStorage persistentStorage,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    public Optional<String> getPersistedConsentId() {
        return Optional.ofNullable(persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    public URL authenticate(String state) {
        ConsentResponse consent =
                apiClient.getConsent(state, credentials.getField(CredentialKeys.USERNAME));
        persistentStorage.put(StorageKeys.CONSENT_ID, consent.getConsentId());
        return new URL(consent.getLinks().getScaRedirect().getHref());
    }

    public void verifyPersistedConsentIdIsValid() {
        Optional.ofNullable(apiClient.getConsentStatus())
                .map(ConsentStatusResponse::getConsentStatus)
                .filter(
                        consentStatus ->
                                consentStatus.equals(DeutscheBankConstants.StatusValues.VALID))
                .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
    }

    public void storeSessionExpiry() {
        ConsentDetailsResponse consentDetailsResponse = apiClient.getConsentDetails();
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }
}
