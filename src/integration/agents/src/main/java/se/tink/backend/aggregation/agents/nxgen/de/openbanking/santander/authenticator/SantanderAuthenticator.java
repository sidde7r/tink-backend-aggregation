package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.configuration.SantanderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SantanderAuthenticator implements Authenticator {

    private final SantanderApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SantanderConfiguration configuration;
    private final String iban;

    public SantanderAuthenticator(
            SantanderApiClient apiClient,
            PersistentStorage persistentStorage,
            SantanderConfiguration configuration,
            String iban) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
        this.iban = iban;
    }

    /*ToDo Add Metrics when flow is done*/
    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        apiClient.setTokenToStorage(apiClient.getToken());
        persistentStorage.put(StorageKeys.CONSENT_ID, apiClient.getConsentId(iban));
    }
}
