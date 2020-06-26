package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CreditAgricoleBaseAuthenticator implements OAuth2Authenticator {

    private final CreditAgricoleBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final CreditAgricoleBaseConfiguration configuration;
    private final String redirecUrl;

    public CreditAgricoleBaseAuthenticator(
            CreditAgricoleBaseApiClient apiClient,
            PersistentStorage persistentStorage,
            AgentConfiguration<CreditAgricoleBaseConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirecUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {
        return apiClient.getToken(code).toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        return apiClient.refreshToken(refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, accessToken);
    }

    private URL getAuthorizeUrl(final String state) {
        final String clientId = configuration.getClientId();
        final String redirectUri = redirecUrl;

        return new URL(configuration.getAuthorizeUrl())
                .queryParam(CreditAgricoleBaseConstants.QueryKeys.CLIENT_ID, clientId)
                .queryParam(
                        CreditAgricoleBaseConstants.QueryKeys.RESPONSE_TYPE,
                        CreditAgricoleBaseConstants.QueryValues.CODE)
                .queryParam(
                        CreditAgricoleBaseConstants.QueryKeys.SCOPE,
                        CreditAgricoleBaseConstants.QueryValues.SCOPE)
                .queryParam(CreditAgricoleBaseConstants.QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(CreditAgricoleBaseConstants.QueryKeys.STATE, state);
    }
}
