package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.configuration.RaiffeisenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RaiffeisenAuthenticator implements Authenticator {

    private final RaiffeisenApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final RaiffeisenConfiguration configuration;

    public RaiffeisenAuthenticator(
            RaiffeisenApiClient apiClient,
            PersistentStorage persistentStorage,
            RaiffeisenConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private RaiffeisenConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        TokenResponse token = apiClient.authenticate();
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token.getAccessToken());

        ConsentResponse consentResponse = apiClient.getConsent();
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
    }
}
