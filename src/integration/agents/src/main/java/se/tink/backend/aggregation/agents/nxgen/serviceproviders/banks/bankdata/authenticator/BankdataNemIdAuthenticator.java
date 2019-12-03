package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.CryptoHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.NemIdAuthenticatorV2;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.NemIdParameters;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.uuid.UUIDUtils;

public class BankdataNemIdAuthenticator implements NemIdAuthenticatorV2 {

    private final BankdataApiClient bankClient;
    private final Storage storage;

    private CryptoHelper cryptoHelper;

    public BankdataNemIdAuthenticator(BankdataApiClient bankClient, Storage storage) {
        this.bankClient = bankClient;
        this.storage = storage;
    }

    @Override
    public NemIdParameters getNemIdParameters() throws AuthenticationException {

        // Generate new keys for a new nemid instantiation
        final String keyPairId = UUIDUtils.generateUUID();
        cryptoHelper = new CryptoHelper(keyPairId);

        // Save keys for use in subsequent logins
        cryptoHelper.persist(storage);

        // initialize nemid
        bankClient.nemIdInit(cryptoHelper);

        // trigger redirect to the endpoint that contains the iframe
        HttpResponse httpResponse = bankClient.portal();

        // parse the response to get the parameters
        return bankClient.fetchNemIdParameters(httpResponse);
    }

    @Override
    public String exchangeNemIdToken(String nemIdToken)
            throws AuthenticationException, AuthorizationException {

        bankClient.eventDoContinue(nemIdToken);
        return bankClient.completeEnrollment(cryptoHelper).getInstallId();
    }

    @Override
    public void authenticateUsingInstallId(String userId, String pinCode, String installId)
            throws SessionException {

        if (cryptoHelper == null) {
            cryptoHelper =
                    CryptoHelper.load(storage).orElseThrow(SessionError.SESSION_EXPIRED::exception);
        }

        bankClient.nemIdInit(cryptoHelper); // Not sure if needed.
        bankClient.loginWithInstallId(userId, pinCode, installId, cryptoHelper);
    }
}
