package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.configuration.EnterCardConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class EnterCardAuthenticator implements OAuth2Authenticator {

    private final Logger logger = LoggerFactory.getLogger(EnterCardAuthenticator.class);

    private final EnterCardApiClient apiClient;
    private final EnterCardConfiguration configuration;
    private final String redirectUrl;

    public EnterCardAuthenticator(
            EnterCardApiClient apiClient,
            AgentConfiguration<EnterCardConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return Urls.AUTHORIZATION
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {
        return apiClient.getToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        logger.info("Entercard - Refreshing access token call");

        // TODO temporary log: input parameter refresh token
        logger.info("Entercard - input parameter refresh token: {}", refreshToken.hashCode());

        OAuth2Token persistedRefreshToken = apiClient.getPersistRefreshToken();
        // TODO temporary log: to trace persist refresh token
        logger.info(
                "Entercard - get persist refresh token: {}",
                persistedRefreshToken.getRefreshToken().get().hashCode());

        OAuth2Token token = apiClient.refreshToken(refreshToken);

        // TODO temporary log to trace new refresh token
        logger.info("Entercard - get new refresh token: {}", token.getRefreshToken().hashCode());
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        apiClient.setTokenToStorage(accessToken);
    }
}
