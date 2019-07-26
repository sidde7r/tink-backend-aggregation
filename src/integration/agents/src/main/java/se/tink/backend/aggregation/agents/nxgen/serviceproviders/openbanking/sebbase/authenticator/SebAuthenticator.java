package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAbstractApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SebAuthenticator implements OAuth2Authenticator {
    private final SebAbstractApiClient client;
    private final SessionStorage sessionStorage;

    public SebAuthenticator(SebAbstractApiClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return client.getAuthorizeUrl()
                .queryParam(
                        SebCommonConstants.QueryKeys.CLIENT_ID,
                        client.getConfiguration().getClientId())
                .queryParam(
                        SebCommonConstants.QueryKeys.RESPONSE_TYPE,
                        SebCommonConstants.QueryValues.RESPONSE_TYPE_TOKEN)
                .queryParam(
                        SebCommonConstants.QueryKeys.SCOPE, SebCommonConstants.QueryValues.SCOPE)
                .queryParam(
                        SebCommonConstants.QueryKeys.REDIRECT_URI,
                        client.getConfiguration().getRedirectUrl())
                .queryParam(SebCommonConstants.QueryKeys.STATE, state)
                .getUrl();
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        TokenRequest request =
                new TokenRequest(
                        client.getConfiguration().getClientId(),
                        client.getConfiguration().getClientSecret(),
                        client.getConfiguration().getRedirectUrl(),
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
                        client.getConfiguration().getClientId(),
                        client.getConfiguration().getClientSecret(),
                        client.getConfiguration().getRedirectUrl());

        OAuth2Token token =
                client.refreshToken(
                        SebCommonConstants.Urls.BASE_URL + SebCommonConstants.Urls.TOKEN, request);
        client.setTokenToSession(token);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        client.setTokenToSession(accessToken);
    }
}
