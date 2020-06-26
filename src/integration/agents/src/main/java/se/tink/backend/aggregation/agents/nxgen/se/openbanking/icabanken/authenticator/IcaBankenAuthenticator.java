package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class IcaBankenAuthenticator implements OAuth2Authenticator {

    private final IcaBankenApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private IcaBankenConfiguration icaBankenConfiguration;
    private String redirectUrl;
    private Credentials credentials;

    public IcaBankenAuthenticator(
            IcaBankenApiClient apiClient,
            PersistentStorage persistentStorage,
            AgentConfiguration<IcaBankenConfiguration> agentConfiguration,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.icaBankenConfiguration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.credentials = credentials;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        final Form params =
                Form.builder()
                        .put(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                        .put(QueryKeys.CLIENT_ID, icaBankenConfiguration.getClientId())
                        .put(QueryKeys.SCOPE, QueryValues.SCOPE)
                        .put(QueryKeys.REDIRECT_URI, redirectUrl)
                        .put(QueryKeys.SSN, credentials.getField(Field.Key.USERNAME))
                        .put(QueryKeys.STATE, state)
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
                        .setRedirectUri(redirectUrl)
                        .build();

        TokenResponse response = apiClient.exchangeAuthorizationCode(request);
        persistentStorage.put(IcaBankenConstants.StorageKeys.TOKEN, response.getAccessToken());
        return response.toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        RefreshTokenRequest request =
                RefreshTokenRequest.builder()
                        .setClientId(icaBankenConfiguration.getClientId())
                        .setGrantType(IcaBankenConstants.QueryValues.REFRESH_TOKEN)
                        .setRefreshToken(refreshToken)
                        .build();
        TokenResponse response = apiClient.exchangeRefreshToken(request);
        persistentStorage.put(IcaBankenConstants.StorageKeys.TOKEN, response.getAccessToken());
        return response.toOauthToken();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}
}
