package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.configuration.EnterCardConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class EnterCardAuthenticator implements Authenticator {

    private final EnterCardApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final EnterCardConfiguration configuration;

    public EnterCardAuthenticator(
            EnterCardApiClient apiClient,
            PersistentStorage persistentStorage,
            EnterCardConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private EnterCardConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        apiClient.getAuth();
    }
}
