package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator;

import com.google.common.base.Strings;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataCryptoHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataCryptoHelperState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataCryptoHelperStateGenerator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.CompleteEnrollResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParameters;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;

@RequiredArgsConstructor
public class BankdataNemIdAuthenticator implements NemIdAuthenticator {

    private final BankdataApiClient bankClient;
    private final Storage storage;
    private final BankdataCryptoHelperStateGenerator stateGenerator;
    private final BankdataCryptoHelper cryptoHelper;

    @Override
    public NemIdParameters getNemIdParameters() {
        resetCryptoHelper();

        // initialize nemid
        bankClient.nemIdInit();

        // trigger redirect to the endpoint that contains the iframe
        HttpResponse httpResponse = bankClient.portal();

        // parse the response to get the parameters
        return bankClient.fetchNemIdParameters(httpResponse);
    }

    private void resetCryptoHelper() {
        // Generate new keys for a new nemid instantiation
        BankdataCryptoHelperState state = stateGenerator.generate();
        // Save keys for use in subsequent logins
        state.saveInStorage(storage);

        cryptoHelper.loadState(state);
    }

    @Override
    public String exchangeNemIdToken(String nemIdToken) {
        bankClient.eventDoContinue(nemIdToken);
        final CompleteEnrollResponse completeEnrollResponse = bankClient.completeEnrollment();
        storage.put(StorageKeys.IDENTITY_DATA, completeEnrollResponse);

        return completeEnrollResponse.getInstallId();
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

        if (!cryptoHelper.isStateInitialized()) {
            BankdataCryptoHelperState state =
                    BankdataCryptoHelperState.loadFromStorage(storage)
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);
            cryptoHelper.loadState(state);
        }

        bankClient.nemIdInit(); // Not sure if needed.
        bankClient.loginWithInstallId(userId, pinCode, installId);
    }
}
