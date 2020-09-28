package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DeutscheBankAuthenticator {

    private final DeutscheBankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final String psuId;

    public DeutscheBankAuthenticator(
            DeutscheBankApiClient apiClient, PersistentStorage persistentStorage, String psuId) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.psuId = psuId;
    }

    public Optional<String> getPersistedConsentId() {
        return Optional.ofNullable(persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    public URL authenticate(String state) {
        ConsentBaseResponse consent = apiClient.getConsent(state, psuId);
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
}
