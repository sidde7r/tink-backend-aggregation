package se.tink.backend.aggregation.agents.nxgen.demo.banks.demoFI.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demoFI.DemoFIApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class DemoFIAuthenticator implements PasswordAuthenticator {
    private DemoFIApiClient client;

    public DemoFIAuthenticator(DemoFIApiClient client) {
        this.client = client;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        // TODO: authenticate stuff

    }
}
