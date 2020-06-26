package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration.SebConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SebAuthenticator implements OAuth2Authenticator {
    private static final Logger log = LoggerFactory.getLogger(SebAuthenticator.class);

    private final SebBaseApiClient client;
    private final SebConfiguration configuration;
    private final String redirectUrl;

    public SebAuthenticator(
            SebBaseApiClient client, AgentConfiguration<SebConfiguration> agentConfiguration) {
        this.client = client;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
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
                .queryParam(SebCommonConstants.QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam(SebCommonConstants.QueryKeys.STATE, state)
                .getUrl();
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        TokenRequest request =
                new TokenRequest(
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        redirectUrl,
                        SebCommonConstants.QueryValues.AUTH_CODE_GRANT,
                        code,
                        SebCommonConstants.QueryValues.SCOPE);

        return client.getToken(request);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        final RefreshRequest request =
                new RefreshRequest(
                        refreshToken,
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        SebCommonConstants.QueryValues.REFRESH_TOKEN_GRANT);
        return client.refreshToken(Urls.OAUTH2_TOKEN, request);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        // will use the tokens from the persistent storage
    }
}
