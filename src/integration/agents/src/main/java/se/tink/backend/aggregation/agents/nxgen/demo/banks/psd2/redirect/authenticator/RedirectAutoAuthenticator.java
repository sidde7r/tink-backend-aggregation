package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;

public class RedirectAutoAuthenticator implements AutoAuthenticator {
    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {}
}
