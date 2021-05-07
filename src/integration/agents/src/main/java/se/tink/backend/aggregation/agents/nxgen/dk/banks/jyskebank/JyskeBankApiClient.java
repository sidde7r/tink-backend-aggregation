package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank;

import java.util.Locale;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.authenticator.rpc.ClientRegistrationRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.authenticator.rpc.ClientRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.authenticator.rpc.OAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.identity.rpc.IdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class JyskeBankApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final RandomValueGenerator randomValueGenerator;
    private String bidCorrId = "";

    public HttpResponse nemIdInit(String codeChallenge) {
        HttpResponse httpResponse =
                client.request(Urls.INIT_AUTH)
                        .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                        .queryParam(QueryKeys.CLIENT_ID, QueryValues.CLIENT_ID)
                        .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                        .queryParam(QueryKeys.REDIRECT_URI, QueryValues.REDIRECT_URI)
                        .queryParam(QueryKeys.RESPONSE_MODE, QueryValues.RESPONSE_MODE)
                        .queryParam(QueryKeys.UI_LOCALES, QueryValues.UI_LOCALES)
                        .queryParam(QueryKeys.ENROLLMENT_CHALLENGE, codeChallenge)
                        .acceptLanguage(Locale.US)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                        .accept(HeaderValues.ACCEPT_HTML)
                        .get(HttpResponse.class);

        // HTTP 302 redirects has been disabled for this agent which is why we have to follow
        // redirects manually here
        String url = httpResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
        httpResponse = client.request(url).get(HttpResponse.class);

        url = httpResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
        return client.request(url).get(HttpResponse.class);
    }

    public HttpResponse validateNemIdToken(String token) {
        return buildTokenRequest(Urls.VALIDATE_NEMID)
                .post(HttpResponse.class, "response=" + EncodingUtils.encodeUrl(token));
    }

    public String fetchToken(String uri, Form form) {
        final HttpResponse httpResponse =
                buildTokenRequest(Urls.AUTH_HOST + uri).post(HttpResponse.class, form.serialize());

        return httpResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
    }

    private RequestBuilder buildTokenRequest(String url) {
        return client.request(url)
                .header(HeaderKeys.ORIGIN, Urls.AUTH_HOST)
                .header(HeaderKeys.REFERER, HeaderValues.REFERER)
                .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .acceptLanguage(Locale.US)
                .accept(HeaderValues.ACCEPT_HTML);
    }

    public ClientRegistrationResponse fetchClientSecret(String token) {
        return client.request(Urls.CLIENT_SECRET)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientRegistrationResponse.class, new ClientRegistrationRequest());
    }

    public OAuthResponse fetchAccessToken(String clientId, String clientSecret, Form oauthForm) {
        final String corrId = randomValueGenerator.generateUuidWithTinkTag();

        return client.request(Urls.OAUTH_TOKEN)
                .header(HeaderKeys.CORR_ID, corrId)
                .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .addBasicAuth(clientId, clientSecret)
                .post(OAuthResponse.class, oauthForm.serialize());
    }

    public ChallengeResponse fetchChallengeCode(String kid) {
        return client.request(Urls.AUTH_CHALLENGE + kid)
                .accept(MediaType.WILDCARD_TYPE)
                .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                .get(ChallengeResponse.class);
    }

    public IdentityResponse fetchIdentityData() {
        return buildRequest(Urls.FETCH_IDENTITY).get(IdentityResponse.class);
    }

    public AccountResponse fetchAccounts() {
        return buildRequest(Urls.FETCH_ACCOUNTS).get(AccountResponse.class);
    }

    public TransactionResponse fetchTransactions(String publicId, int page) {
        return buildRequest(Urls.FETCH_TRANSACTIONS)
                .queryParam(QueryKeys.PUBLIC_ID, publicId)
                .queryParam(QueryKeys.PAGE, Integer.toString(page))
                .accept(HeaderValues.ACCEPT_JSON)
                .get(TransactionResponse.class);
    }

    private RequestBuilder buildRequest(String url) {
        if (bidCorrId.isEmpty()) {
            bidCorrId = randomValueGenerator.generateUuidWithTinkTag();
        }

        return client.request(url)
                .header(HeaderKeys.BD_CORRELATION, bidCorrId)
                .header(HeaderKeys.API_KEY, HeaderValues.API_KEY)
                .header(HeaderKeys.APP_VERSION, HeaderValues.APP_VERSION)
                .header(
                        HeaderKeys.AUTHORIZATION,
                        "Bearer " + sessionStorage.get(Storage.ACCESS_TOKEN))
                .accept(MediaType.WILDCARD_TYPE);
    }

    public String validateVersion(String correlationId) {
        return buildServerStatusRequest(Urls.VALIDATE_VERSION, correlationId)
                .header(HeaderKeys.BUILD_NUMBER, HeaderValues.BUILD_NUMBER)
                .get(String.class);
    }

    public String serverStatus(String correlationId) {
        return buildServerStatusRequest(Urls.SERVER_STATUS, correlationId).get(String.class);
    }

    public String generalHealth(String correlationId) {
        return buildServerStatusRequest(Urls.GENERAL_HEALTH, correlationId).get(String.class);
    }

    private RequestBuilder buildServerStatusRequest(String url, String correlationId) {
        return client.request(url)
                .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                .header(HeaderKeys.API_KEY, HeaderValues.API_KEY)
                .header(HeaderKeys.CORRELATION_ID, correlationId)
                .accept(MediaType.WILDCARD_TYPE);
    }
}
