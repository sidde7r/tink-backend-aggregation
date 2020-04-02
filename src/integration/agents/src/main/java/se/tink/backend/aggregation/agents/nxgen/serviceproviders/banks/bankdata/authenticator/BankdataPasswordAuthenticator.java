package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.storage.BankdataStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;

public class BankdataPasswordAuthenticator implements AutoAuthenticator {

    private final BankdataNemIdAuthenticator authenticator;
    private final String username;
    private final String pinCode;
    private final BankdataStorage storage;

    public BankdataPasswordAuthenticator(
            String username,
            String pinCode,
            BankdataNemIdAuthenticator authenticator,
            BankdataStorage storage) {
        this.username = username;
        this.pinCode = pinCode;
        this.authenticator = authenticator;
        this.storage = storage;
    }

    @Override
    public void autoAuthenticate()
            throws BankServiceException, SessionException, LoginException, AuthorizationException {

        final String installId =
                storage.getNemidInstallId().orElseThrow(SessionError.SESSION_EXPIRED::exception);

        authenticator.authenticateUsingInstallId(username, pinCode, installId);
    }
}
