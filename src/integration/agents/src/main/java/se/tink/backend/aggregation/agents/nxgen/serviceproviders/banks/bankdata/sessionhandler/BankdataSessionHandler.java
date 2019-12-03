package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.BankdataNemIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class BankdataSessionHandler implements SessionHandler {

    private final String username;
    private final String pinCode;
    private final BankdataNemIdAuthenticator authenticator;
    private final Storage storage;

    public BankdataSessionHandler(
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
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {

        if (!canAutoAuthenticate()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {

            authenticator.authenticateUsingInstallId(
                    username, pinCode, storage.get(BankdataConstants.Storage.NEMID_INSTALL_ID));
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private boolean canAutoAuthenticate() {
        return storage.containsKey(BankdataConstants.Storage.NEMID_INSTALL_ID);
    }
}
