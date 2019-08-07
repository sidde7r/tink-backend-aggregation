package se.tink.backend.aggregation.nxgen.controllers.authentication.automatic;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;

public interface AutoAuthenticator {
    void autoAuthenticate(Credentials credentials)
            throws SessionException, BankServiceException, AuthorizationException;
}
