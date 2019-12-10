package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.UnicreditApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AuthenticationControllerType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class UnicreditAuthenticator implements Authenticator, AuthenticationControllerType {

    private final UnicreditApiClient apiClient;

    public UnicreditAuthenticator(UnicreditApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        apiClient.authenticate();
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        // since authenticate always opens the third party app
        return true;
    }
}
