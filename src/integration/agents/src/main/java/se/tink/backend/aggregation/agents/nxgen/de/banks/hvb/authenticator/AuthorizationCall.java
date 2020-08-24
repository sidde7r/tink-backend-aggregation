package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.ConfigurationProvider;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;

public class AuthorizationCall extends SimpleExternalApiCall<AuthenticationData, String> {

    private static final String RESPONSE_TYPE = "code";
    private static final String REDIRECT_URL = "https://mfpredirecturi";
    private static final String AUTHORIZATION_CODE_URL_PREFIX =
            format("^%s\\?%s=", REDIRECT_URL, RESPONSE_TYPE);

    private final ConfigurationProvider configurationProvider;

    public AuthorizationCall(
            TinkHttpClient httpClient, ConfigurationProvider configurationProvider) {
        super(httpClient);
        this.configurationProvider = configurationProvider;
    }

    @Override
    protected HttpRequest prepareRequest(AuthenticationData authData) {
        return new HttpRequestImpl(
                HttpMethod.GET,
                new URL(configurationProvider.getBaseUrl() + "/az/v1/authorization")
                        .queryParams(prepareQueryParams(authData)),
                prepareRequestHeaders(),
                null);
    }

    private Map<String, String> prepareQueryParams(AuthenticationData authData) {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", authData.getClientId());
        params.put("redirect_uri", REDIRECT_URL);
        params.put("response_type", RESPONSE_TYPE);
        params.put("scope", "RegisteredClient UCAuthenticatedUser");
        return params;
    }

    private MultivaluedMap<String, Object> prepareRequestHeaders() {
        MultivaluedMap<String, Object> headers = configurationProvider.getStaticHeaders();
        headers.putSingle(ACCEPT, "text/javascript, text/html, application/xml, text/xml, */*");
        headers.putSingle(CONTENT_TYPE, APPLICATION_JSON);
        return headers;
    }

    @Override
    protected ExternalApiCallResult<String> parseResponse(HttpResponse httpResponse) {
        return ExternalApiCallResult.of(getCode(httpResponse), httpResponse.getStatus());
    }

    private String getCode(HttpResponse httpResponse) {
        return Optional.ofNullable(httpResponse)
                .map(HttpResponse::getLocation)
                .map(URI::toString)
                .map(this::removePrefix)
                .orElseThrow(() -> new IllegalArgumentException("Couldn't extract code."));
    }

    private String removePrefix(String path) {
        return path.replaceFirst(AUTHORIZATION_CODE_URL_PREFIX, "");
    }
}
