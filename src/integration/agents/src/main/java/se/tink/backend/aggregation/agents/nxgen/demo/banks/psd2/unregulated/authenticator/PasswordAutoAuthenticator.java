package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.unregulated.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;

public class PasswordAutoAuthenticator implements AutoAuthenticator {

    @Override
    public void autoAuthenticate(Credentials credentials)
            throws SessionException, BankServiceException {
        // nop
    }
}
