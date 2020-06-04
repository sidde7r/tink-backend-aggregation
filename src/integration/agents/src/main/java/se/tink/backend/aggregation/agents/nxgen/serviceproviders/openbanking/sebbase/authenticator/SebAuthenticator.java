package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator;

import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration.SebConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SebAuthenticator implements OAuth2Authenticator {
    private static final Logger log = LoggerFactory.getLogger(SebAuthenticator.class);

    private final SebBaseApiClient client;
    private final SebConfiguration configuration;

    public SebAuthenticator(SebBaseApiClient client, SebConfiguration configuration) {
        this.client = client;
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
                        SebCommonConstants.QueryValues.AUTH_CODE_GRANT,
                        code,
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
                        SebCommonConstants.QueryValues.REFRESH_TOKEN_GRANT);

        try {
            return client.refreshToken(Urls.OAUTH2_TOKEN, request);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                ErrorResponse error = e.getResponse().getBody(ErrorResponse.class);
                if (error.isInvalidGrant()) {
                    log.warn("Invalid refresh token.");
                    throw SessionError.SESSION_EXPIRED.exception();
                }
            }
            throw e;
        }
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        // will use the tokens from the persistent storage
    }
}
