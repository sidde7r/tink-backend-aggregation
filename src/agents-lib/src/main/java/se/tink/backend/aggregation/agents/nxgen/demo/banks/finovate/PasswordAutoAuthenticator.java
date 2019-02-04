package se.tink.backend.aggregation.agents.nxgen.demo.banks.finovate;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;

/*
This is a temporary solution and should be deleted as soon as the demo is done
 */
public class PasswordAutoAuthenticator implements AutoAuthenticator {

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        // nop
    }
}
