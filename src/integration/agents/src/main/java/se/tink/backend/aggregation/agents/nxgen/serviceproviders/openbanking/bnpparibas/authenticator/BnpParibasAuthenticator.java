package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BnpParibasAuthenticator implements OAuth2Authenticator {

    private final SessionStorage sessionStorage;
    private final BnpParibasApiBaseClient apiClient;
    private BnpParibasConfiguration bnpParibasConfiguration;
    private String redirectUrl;

    public BnpParibasAuthenticator(
            BnpParibasApiBaseClient apiClient,
            SessionStorage sessionStorage,
            AgentConfiguration<BnpParibasConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.bnpParibasConfiguration = agentConfiguration.getClientConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        TokenRequest request =
                TokenRequest.builder()
                        .setClientId(bnpParibasConfiguration.getClientId())
                        .setCode(code)
                        .setGrantType(BnpParibasBaseConstants.QueryValues.AUTHORIZATION_CODE)
                        .setRedirectUri(redirectUrl)
                        .build();

        return apiClient.exchangeAuthorizationToken(request).toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        RefreshRequest refreshRequest =
                new RefreshRequest(
                        refreshToken,
                        bnpParibasConfiguration.getClientId(),
                        BnpParibasBaseConstants.QueryValues.REFRESH_TOKEN);
        return apiClient.exchangeRefreshToken(refreshRequest);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(BnpParibasBaseConstants.StorageKeys.TOKEN, accessToken);
    }
}
