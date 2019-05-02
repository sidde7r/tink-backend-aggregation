package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class RedsysApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private RedsysConfiguration configuration;

    public RedsysApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private RedsysConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(RedsysConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public URL getAuthorizeUrl(String state, String codeChallenge) {
        final String baseAuthUrl =
                getConfiguration().getBaseUrl() + "/" + getConfiguration().getAspsp();
        final String clientId = getConfiguration().getAuthClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        return client.request(baseAuthUrl + RedsysConstants.Urls.OAUTH)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .getUrl();
    }

    public OAuth2Token getToken(String code, String codeVerifier) {
        final String url =
                getConfiguration().getBaseUrl() + "/" + getConfiguration().getAspsp() + Urls.TOKEN;
        final String clientId = getConfiguration().getAuthClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        final String payload =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.AUTHORIZATION_CODE)
                        .put(FormKeys.CLIENT_ID, clientId)
                        .put(FormKeys.CODE, code)
                        .put(FormKeys.REDIRECT_URI, redirectUri)
                        .put(FormKeys.CODE_VERIFIER, codeVerifier)
                        .build()
                        .serialize();

        return client.request(url)
                .body(payload, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token refreshToken(final String refreshToken) {
        final String url = getConfiguration().getBaseUrl() + "/" + Urls.REFRESH;
        final String aspsp = getConfiguration().getAspsp();
        final String clientId = getConfiguration().getAuthClientId();

        final String payload =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN)
                        .put(FormKeys.ASPSP, aspsp)
                        .put(FormKeys.CLIENT_ID, clientId)
                        .put(FormKeys.REFRESH_TOKEN, refreshToken)
                        .build()
                        .serialize();

        return client.request(url)
                .body(payload, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class)
                .toTinkToken();
    }
}
