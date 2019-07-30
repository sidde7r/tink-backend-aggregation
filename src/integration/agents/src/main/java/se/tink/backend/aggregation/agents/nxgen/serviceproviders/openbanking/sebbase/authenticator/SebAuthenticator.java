package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration.SebConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SebAuthenticator implements OAuth2Authenticator {
    private final SebBaseApiClient client;
    private final SessionStorage sessionStorage;
    private final SebConfiguration configuration;

    public SebAuthenticator(
            SebBaseApiClient client,
            SessionStorage sessionStorage,
            SebConfiguration configuration) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.configuration = configuration;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return client.getAuthorizeUrl()
                .queryParam(SebCommonConstants.QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(
                        SebCommonConstants.QueryKeys.RESPONSE_TYPE,
                        SebCommonConstants.QueryValues.RESPONSE_TYPE_TOKEN)
                .queryParam(
                        SebCommonConstants.QueryKeys.SCOPE, SebCommonConstants.QueryValues.SCOPE)
                .queryParam(
                        SebCommonConstants.QueryKeys.REDIRECT_URI, configuration.getRedirectUrl())
                .queryParam(SebCommonConstants.QueryKeys.STATE, state)
                .getUrl();
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        TokenRequest request =
                new TokenRequest(
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        configuration.getRedirectUrl(),
                        code,
                        SebCommonConstants.QueryValues.GRANT_TYPE,
                        SebCommonConstants.QueryValues.SCOPE);

        return client.getToken(request);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        RefreshRequest request =
                new RefreshRequest(
                        refreshToken,
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        configuration.getRedirectUrl());

        OAuth2Token token =
                client.refreshToken(
                        SebCommonConstants.Urls.BASE_URL.concat(SebCommonConstants.Urls.TOKEN),
                        request);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(SebCommonConstants.StorageKeys.TOKEN, accessToken);
    }
}
