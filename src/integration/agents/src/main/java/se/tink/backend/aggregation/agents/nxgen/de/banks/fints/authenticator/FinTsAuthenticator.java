package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.authenticator;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.general.GeneralClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

@AllArgsConstructor
public class FinTsAuthenticator implements PasswordAuthenticator {

    private final GeneralClient generalClient;

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        generalClient.initializeSession();
        generalClient.finish();
        generalClient.initializeDialog();
    }
}
