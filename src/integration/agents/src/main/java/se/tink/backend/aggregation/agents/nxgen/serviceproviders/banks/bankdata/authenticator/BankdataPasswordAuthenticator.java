package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class BankdataPasswordAuthenticator implements AutoAuthenticator {

    private final BankdataNemIdAuthenticator authenticator;
    private final String username;
    private final String pinCode;
    private final Storage storage;

    public BankdataPasswordAuthenticator(
            String username,
            String pinCode,
            BankdataNemIdAuthenticator authenticator,
            Storage storage) {
        this.username = username;
        this.pinCode = pinCode;
        this.authenticator = authenticator;
        this.storage = storage;
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {

        authenticator.authenticateUsingInstallId(
                username, pinCode, storage.get(BankdataConstants.Storage.NEMID_INSTALL_ID));
    }
}
