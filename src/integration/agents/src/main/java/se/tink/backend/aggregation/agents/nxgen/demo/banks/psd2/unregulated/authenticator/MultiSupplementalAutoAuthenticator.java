package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.unregulated.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;

public class MultiSupplementalAutoAuthenticator implements AutoAuthenticator {

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        // nop
    }
}
