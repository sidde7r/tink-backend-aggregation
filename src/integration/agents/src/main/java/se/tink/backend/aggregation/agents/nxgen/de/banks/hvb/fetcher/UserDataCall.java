package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;

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

public class UserDataCall extends SimpleExternalApiCall<String, UserDataResponse> {

    private final ConfigurationProvider configurationProvider;

    public UserDataCall(TinkHttpClient httpClient, ConfigurationProvider configurationProvider) {
        super(httpClient);
        this.configurationProvider = configurationProvider;
    }

    @Override
    protected HttpRequest prepareRequest(String authorization) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(
                        configurationProvider.getBaseUrl()
                                + "/adapters/UC_MBX_GL_BE_FACADE_NJ/userDataList"),
                prepareRequestHeaders(authorization),
                null);
    }

    private MultivaluedMap<String, Object> prepareRequestHeaders(String authorization) {
        MultivaluedMap<String, Object> headers = configurationProvider.getStaticHeaders();
        headers.putSingle(AUTHORIZATION, authorization);
        headers.putSingle(CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
        return headers;
    }

    @Override
    protected ExternalApiCallResult<UserDataResponse> parseResponse(HttpResponse httpResponse) {
        return ExternalApiCallResult.of(
                httpResponse.getBody(UserDataResponse.class), httpResponse.getStatus());
    }
}
