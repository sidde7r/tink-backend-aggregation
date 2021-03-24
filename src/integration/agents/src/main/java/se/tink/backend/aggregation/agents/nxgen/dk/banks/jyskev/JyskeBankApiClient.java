package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev;

import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.JyskeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.JyskeConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.JyskeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.JyskeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.JyskeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.authenticator.rpc.ClientRegistrationRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.authenticator.rpc.ClientRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.authenticator.rpc.OAuthResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class JyskeBankApiClient {
    private final TinkHttpClient client;

    public JyskeBankApiClient(TinkHttpClient client) {
        this.client = client;
    }

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
                        .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderValues.ACCEPT_LANGUAGE)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                        .accept(HeaderValues.ACCEPT)
                        .get(HttpResponse.class);

        String url = httpResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
        httpResponse = client.request(url).get(HttpResponse.class);

        url = httpResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
        return client.request(url).get(HttpResponse.class);
    }

    public HttpResponse validateNemIdToken(String token) {
        return client.request(Urls.VALIDATE_NEMID)
                .header(HeaderKeys.ORIGIN, Urls.AUTH_HOST)
                .header(HeaderKeys.REFERER, HeaderValues.REFERER)
                .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderValues.ACCEPT_LANGUAGE)
                .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(HeaderValues.ACCEPT)
                .post(HttpResponse.class, "response=" + EncodingUtils.encodeUrl(token));
    }

    public String fetchToken(String uri, Form form) {
        final HttpResponse httpResponse =
                client.request(Urls.AUTH_HOST + uri)
                        .header(HeaderKeys.ORIGIN, Urls.AUTH_HOST)
                        .header(HeaderKeys.REFERER, HeaderValues.REFERER)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderValues.ACCEPT_LANGUAGE)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(HeaderValues.ACCEPT)
                        .post(HttpResponse.class, form.serialize());

        return httpResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
    }

    public ClientRegistrationResponse fetchClientSecret(String token) {
        return client.request(Urls.CLIENT_SECRET)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientRegistrationResponse.class, new ClientRegistrationRequest());
    }

    public OAuthResponse fetchAccessToken(String clientId, String clientSecret, Form oauthForm) {
        final UUID corrId = UUID.randomUUID();

        return client.request(Urls.OAUTH_TOKEN)
                .header(HeaderKeys.CORR_ID, corrId.toString())
                .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .addBasicAuth(clientId, clientSecret)
                .post(OAuthResponse.class, oauthForm.serialize());
    }

    public String validateVersion(String correlationId) {
        return client.request(Urls.VALIDATE_VERSION)
                .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                .header(HeaderKeys.API_KEY, HeaderValues.API_KEY)
                .header(HeaderKeys.BUILD_NUMBER, HeaderValues.BUILD_NUMBER)
                .header(HeaderKeys.CORRELATION_ID, correlationId)
                .accept(MediaType.WILDCARD_TYPE)
                .get(String.class);
    }

    public String serverStatus(String correlationId) {
        return client.request(Urls.SERVER_STATUS)
                .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                .header(HeaderKeys.API_KEY, HeaderValues.API_KEY)
                .header(HeaderKeys.CORRELATION_ID, correlationId)
                .accept(MediaType.WILDCARD_TYPE)
                .get(String.class);
    }

    public String generalHealth(String correlationId) {
        return client.request(Urls.GENERAL_HEALTH)
                .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                .header(HeaderKeys.API_KEY, HeaderValues.API_KEY)
                .header(HeaderKeys.CORRELATION_ID, correlationId)
                .accept(MediaType.WILDCARD_TYPE)
                .get(String.class);
    }
}
