package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.general.GeneralClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class FinTsAuthenticator implements PasswordAuthenticator {

    private GeneralClient generalClient;

    public FinTsAuthenticator(GeneralClient generalClient) {
        this.generalClient = generalClient;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        generalClient.initializeSession();
        generalClient.finish();
        generalClient.initializeDialog();
    }
}
