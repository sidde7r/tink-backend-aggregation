package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator;

import com.google.common.base.Strings;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.CryptoHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParameters;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.uuid.UUIDUtils;

public class BankdataNemIdAuthenticator implements NemIdAuthenticator {

    private final BankdataApiClient bankClient;
    private final Storage storage;

    private CryptoHelper cryptoHelper;

    public BankdataNemIdAuthenticator(BankdataApiClient bankClient, Storage storage) {
        this.bankClient = bankClient;
        this.storage = storage;
    }

    @Override
    public NemIdParameters getNemIdParameters() {

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
    public String exchangeNemIdToken(String nemIdToken) {
        bankClient.eventDoContinue(nemIdToken);
        return bankClient.completeEnrollment(cryptoHelper).getInstallId();
    }

    @Override
    public void authenticateUsingInstallId(
            @Nonnull String userId, @Nonnull String pinCode, @Nonnull String installId)
            throws SessionException, LoginException, AuthorizationException {

        if (Strings.isNullOrEmpty(installId)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        if (Strings.isNullOrEmpty(userId) || Strings.isNullOrEmpty(pinCode)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        if (cryptoHelper == null) {
            cryptoHelper =
                    CryptoHelper.load(storage).orElseThrow(SessionError.SESSION_EXPIRED::exception);
        }

        bankClient.nemIdInit(cryptoHelper); // Not sure if needed.
        bankClient.loginWithInstallId(userId, pinCode, installId, cryptoHelper);
    }
}
