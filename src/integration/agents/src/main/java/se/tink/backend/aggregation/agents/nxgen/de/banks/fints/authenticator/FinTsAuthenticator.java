package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.authenticator;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.dialog.DialogClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

@AllArgsConstructor
public class FinTsAuthenticator implements PasswordAuthenticator {

    private final DialogClient dialogClient;

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        dialogClient.initializeSession();
        dialogClient.finish();
        dialogClient.initializeDialog();
    }
}
