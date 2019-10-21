package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.configuration.CreditAgricoleConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CreditAgricoleAuthenticator implements OAuth2Authenticator {

    private final CreditAgricoleApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final CreditAgricoleConfiguration configuration;

    public CreditAgricoleAuthenticator(
            CreditAgricoleApiClient apiClient,
            PersistentStorage persistentStorage,
            CreditAgricoleConfiguration configuration) {

        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        //TODO refactor exception
        AuthorizePointsEnum bank = persistentStorage.get(CreditAgricoleConstants.StorageKeys.BANK_URL, AuthorizePointsEnum.class).orElseThrow(() -> new RuntimeException("Unable to load correct bank url"));


        final String clientId = configuration.getClientId();
        final String redirectUri = configuration.getRedirectUrl();

        System.out.println("https://127.0.0.1:7357/api/v1/thirdparty/callback?state="+state+"&code="+state);

        return new URL(bank.getAuthUrl())
                        .queryParam(CreditAgricoleConstants.QueryKeys.CLIENT_ID, clientId)
                        .queryParam(
                                CreditAgricoleConstants.QueryKeys.RESPONSE_TYPE,
                                CreditAgricoleConstants.QueryValues.CODE)
                        .queryParam(
                                CreditAgricoleConstants.QueryKeys.SCOPE,
                                CreditAgricoleConstants.QueryValues.SCOPE)
                        .queryParam(CreditAgricoleConstants.QueryKeys.REDIRECT_URI, redirectUri)
                        .queryParam(CreditAgricoleConstants.QueryKeys.STATE, state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {
        TokenResponse tokenResponse = apiClient.getToken(code);
        return tokenResponse.toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        TokenResponse tokenResponse = apiClient.refreshToken(refreshToken);
        OAuth2Token token = tokenResponse.toTinkToken();
        apiClient.setTokenToSession(token);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(CreditAgricoleConstants.StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
