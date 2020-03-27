package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class FinTsAuthenticator implements PasswordAuthenticator {

    public FinTsAuthenticator() {}

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        throw new NotImplementedException("Will be covered in next PR");
    }
}
