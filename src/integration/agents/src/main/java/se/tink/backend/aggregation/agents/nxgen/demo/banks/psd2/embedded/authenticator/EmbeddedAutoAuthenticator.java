package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.embedded.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;

public class EmbeddedAutoAuthenticator implements AutoAuthenticator {

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        // nop
    }
}
