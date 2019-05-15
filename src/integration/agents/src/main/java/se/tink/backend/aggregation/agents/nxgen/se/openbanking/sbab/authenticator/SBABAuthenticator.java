package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.configuration.SBABConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SBABAuthenticator implements Authenticator {

    private final SBABApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SBABConfiguration configuration;

    public SBABAuthenticator(
            SBABApiClient apiClient,
            PersistentStorage persistentStorage,
            SBABConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private SBABConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        persistentStorage.put(
                CredentialKeys.USERNAME, credentials.getField(CredentialKeys.USERNAME));
        persistentStorage.put(
                CredentialKeys.PASSWORD, credentials.getField(CredentialKeys.PASSWORD));
    }
}
