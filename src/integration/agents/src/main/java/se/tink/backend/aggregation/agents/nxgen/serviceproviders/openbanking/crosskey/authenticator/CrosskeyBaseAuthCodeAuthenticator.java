package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator;

import java.util.function.Function;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CrosskeyBaseAuthCodeAuthenticator implements OAuth2Authenticator {

    private final CrosskeyBaseApiClient apiClient;
    private final Function<String, URL> getAuthorizeUrl;

    private CrosskeyBaseAuthCodeAuthenticator(
            CrosskeyBaseApiClient apiClient, Function<String, URL> getAuthorizeUrl) {
        this.apiClient = apiClient;
        this.getAuthorizeUrl = getAuthorizeUrl;
    }

    public static CrosskeyBaseAuthCodeAuthenticator getInstanceForAis(
            CrosskeyBaseApiClient apiClient) {
        return new CrosskeyBaseAuthCodeAuthenticator(apiClient, apiClient::getAisAuthorizeUrl);
    }

    public static CrosskeyBaseAuthCodeAuthenticator getInstanceForPis(
            CrosskeyBaseApiClient apiClient) {
        return new CrosskeyBaseAuthCodeAuthenticator(apiClient, apiClient::getPisAuthorizeUrl);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return getAuthorizeUrl.apply(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return apiClient.getToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws BankServiceException {
        return apiClient.getRefreshToken(refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        apiClient.setTokenToSession(accessToken);
    }
}
