package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.configuration.AhoiSandboxConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class AhoiSandboxAuthenticator implements PasswordAuthenticator {

    private final AhoiSandboxApiClient apiClient;
    private final AhoiSandboxConfiguration configuration;

    public AhoiSandboxAuthenticator(
            AhoiSandboxApiClient apiClient, AhoiSandboxConfiguration configuration) {
        this.apiClient = apiClient;
        this.configuration = configuration;
    }

    private AhoiSandboxConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        Credentials credentials = new Credentials();
        credentials.setUsername(username);
        credentials.setPassword(password);

        apiClient.authenticate(credentials);
    }
}
