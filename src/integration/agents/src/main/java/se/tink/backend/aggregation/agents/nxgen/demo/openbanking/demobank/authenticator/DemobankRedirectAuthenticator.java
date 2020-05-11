package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParamsValues;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DemobankRedirectAuthenticator implements OAuth2Authenticator {

    private final DemobankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private final String callbackUri;

    public DemobankRedirectAuthenticator(
            DemobankApiClient apiClient,
            PersistentStorage persistentStorage,
            Credentials credentials,
            String callbackUri) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.callbackUri = callbackUri;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return new URL(Urls.BASE_URL)
                .concat(Urls.OAUTH_AUTHORIZE)
                .queryParam(QueryParams.RESPONSE_TYPE, QueryParamsValues.RESPONSE_TYPE)
                .queryParam(QueryParams.CLIENT_ID, QueryParamsValues.CLIENT_ID)
                .queryParam(QueryParams.STATE, state)
                .queryParam(QueryParams.REDIRECT_URI, callbackUri);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {

        final OAuth2Token token = apiClient.getToken(code);
        return token;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        try {
            final OAuth2Token token = this.apiClient.refreshToken(refreshToken);
            apiClient.setTokenToSession(token);
            return token;
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        apiClient.setTokenToSession(accessToken);
    }
}
