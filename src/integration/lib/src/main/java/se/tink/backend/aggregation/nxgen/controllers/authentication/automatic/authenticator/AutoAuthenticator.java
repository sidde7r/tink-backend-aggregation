package se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;

public interface AutoAuthenticator {
    void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException;
}
