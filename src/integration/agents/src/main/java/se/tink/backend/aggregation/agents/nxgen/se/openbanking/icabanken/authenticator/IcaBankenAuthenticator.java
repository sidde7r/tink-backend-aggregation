package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IcaBankenAuthenticator implements OAuth2Authenticator {

    private final IcaBankenApiClient apiClient;
    private final SessionStorage sessionStorage;
    private IcaBankenConfiguration icaBankenConfiguration;
    private Credentials credentials;

    public IcaBankenAuthenticator(
            IcaBankenApiClient apiClient,
            SessionStorage sessionStorage,
            IcaBankenConfiguration icaBankenConfiguration,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.icaBankenConfiguration = icaBankenConfiguration;
        this.credentials = credentials;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        final Form params =
                Form.builder()
                        .put(
                                IcaBankenConstants.QueryKeys.RESPONSE_TYPE,
                                IcaBankenConstants.QueryValues.CODE)
                        .put(
                                IcaBankenConstants.QueryKeys.CLIENT_ID,
                                icaBankenConfiguration.getClientId())
                        .put(
                                IcaBankenConstants.QueryKeys.SCOPE,
                                IcaBankenConstants.QueryValues.SCOPE)
                        .put(
                                IcaBankenConstants.QueryKeys.REDIRECT_URI,
                                icaBankenConfiguration.getRedirectUri())
                        .put(IcaBankenConstants.QueryKeys.STATE, state)
                        .put(
                                IcaBankenConstants.QueryKeys.SSN,
                                credentials.getField(Field.Key.USERNAME))
                        .build();

        return new URL(IcaBankenConstants.ProductionUrls.AUTH_PATH + "?" + params.toString());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        AuthorizationRequest request =
                AuthorizationRequest.builder()
                        .setClientId(icaBankenConfiguration.getClientId())
                        .setCode(code)
                        .setGrantType(IcaBankenConstants.QueryValues.AUTHORIZATION_CODE)
                        .setRedirectUri(icaBankenConfiguration.getRedirectUri())
                        .build();

        TokenResponse response = apiClient.exchangeAuthorizationCode(request);
        sessionStorage.put(IcaBankenConstants.StorageKeys.TOKEN, response.getAccessToken());
        return response.toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        RefreshTokenRequest request =
                RefreshTokenRequest.builder()
                        .setClientId(icaBankenConfiguration.getClientId())
                        .setGrantType(IcaBankenConstants.QueryValues.REFRESH_TOKEN)
                        .setScope(IcaBankenConstants.QueryValues.ACCOUNT)
                        .setRefreshToken(refreshToken)
                        .build();
        TokenResponse response = apiClient.exchangeRefreshToken(request);
        sessionStorage.put(IcaBankenConstants.StorageKeys.TOKEN, response.getAccessToken());
        return response.toOauthToken();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}
}
