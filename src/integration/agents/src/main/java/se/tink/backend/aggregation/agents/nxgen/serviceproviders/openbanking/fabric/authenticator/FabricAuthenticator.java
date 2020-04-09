package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc.CreateConsentResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class FabricAuthenticator {

    private final FabricApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public FabricAuthenticator(FabricApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    public URL buildAuthorizeUrl(String state) {
        CreateConsentResponse consentResponse = apiClient.getConsent(state);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        return new URL(consentResponse.getAuthorizeUrl());
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        return apiClient.getConsentStatus(consentId);
    }
}
