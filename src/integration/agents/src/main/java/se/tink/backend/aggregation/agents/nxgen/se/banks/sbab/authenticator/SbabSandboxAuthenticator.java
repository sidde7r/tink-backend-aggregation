package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

public class SbabSandboxAuthenticator implements Authenticator {

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {}
}
