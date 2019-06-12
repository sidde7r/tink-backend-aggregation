package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BelfiusAuthenticator implements PasswordAuthenticator {

    private final BelfiusApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final BelfiusConfiguration configuration;

    public BelfiusAuthenticator(
            BelfiusApiClient apiClient,
            PersistentStorage persistentStorage,
            BelfiusConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {}
}
