package se.tink.backend.aggregation.agents.nxgen.demo.banks.demoFI.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demoFI.DemoFakeBankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class DemoFakeBankAuthenticator implements PasswordAuthenticator {
    private DemoFakeBankApiClient client;

    public DemoFakeBankAuthenticator(DemoFakeBankApiClient client) {
        this.client = client;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        // TODO: authenticate stuff

    }
}
