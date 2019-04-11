package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.AccountSetupResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.ClientCredentialTokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class ICSOAuthAuthenticator implements OAuth2Authenticator {

    private final ICSApiClient client;
    private final SessionStorage sessionStorage;

    public ICSOAuthAuthenticator(ICSApiClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        final String accountRequestId =
                Optional.ofNullable(client.fetchTokenWithClientCredential())
                        .map(ClientCredentialTokenResponse::toTinkToken)
                        .map(client::setupAccount)
                        .filter(AccountSetupResponse::receivedAllReadPermissions)
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.MISSING_PERMISSIONS))
                        .getData()
                        .getAccountRequestId();

        sessionStorage.put(StorageKeys.STATE, state);

        return client.createAuthorizeRequest(state, accountRequestId).getUrl();
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return client.fetchToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        return client.refreshToken(refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        this.client.setToken(accessToken);
    }
}
