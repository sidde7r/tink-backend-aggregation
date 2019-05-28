package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.configuration.SantanderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SantanderAuthenticator implements Authenticator {

    private final SantanderApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SantanderConfiguration configuration;

    public SantanderAuthenticator(
            SantanderApiClient apiClient,
            PersistentStorage persistentStorage,
            SantanderConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private SantanderConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        apiClient.setTokenToStorage(apiClient.getToken("12345"));
        persistentStorage.put(StorageKeys.CONSENT_ID, apiClient.getConsentId());
    }
}
