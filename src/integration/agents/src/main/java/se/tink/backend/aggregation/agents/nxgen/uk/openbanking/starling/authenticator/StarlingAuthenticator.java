package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.CodeExchangeForm;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.TokenRefreshForm;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.entity.ClientConfigurationEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public class StarlingAuthenticator implements OAuth2Authenticator {

    private final StarlingApiClient apiClient;
    private final ClientConfigurationEntity configuration;

    public StarlingAuthenticator(
            StarlingApiClient apiClient, ClientConfigurationEntity configuration) {
        this.apiClient = apiClient;
        this.configuration = configuration;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return new URL(StarlingConstants.Url.AUTH_STARLING)
                .queryParam(StarlingConstants.RequestKey.CLIENT_ID, configuration.getClientId())
                .queryParam(
                        StarlingConstants.RequestKey.REDIRECT_URI, configuration.getRedirectUrl())
                .queryParam(
                        StarlingConstants.RequestKey.RESPONSE_TYPE,
                        StarlingConstants.RequestValue.CODE)
                .queryParam(StarlingConstants.RequestKey.STATE, state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {

        CodeExchangeForm exchangeForm =
                CodeExchangeForm.builder()
                        .withCode(code)
                        .asClient(configuration.getClientId(), configuration.getClientSecret())
                        .withRedirect(configuration.getRedirectUrl())
                        .build();

        return apiClient.exchangeCode(exchangeForm);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        TokenRefreshForm refreshForm =
                TokenRefreshForm.builder()
                        .withRefreshToken(refreshToken)
                        .asClient(configuration.getClientId(), configuration.getClientSecret())
                        .build();

        return apiClient.refreshAccessToken(refreshForm);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        /* Using persistent storage instead. */
    }
}
