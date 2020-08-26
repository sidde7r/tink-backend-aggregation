package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenBaseUrl;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenClientId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenCode;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenStaticHeaders;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.GET;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.ConfigurationProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public class AuthorizationCallTest {

    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);

    private AuthorizationCall tested = new AuthorizationCall(httpClient, configurationProvider);

    @Test
    public void prepareRequestShouldReturnHttpRequestForValidArg() {
        // given
        when(configurationProvider.getBaseUrl()).thenReturn(givenBaseUrl());
        when(configurationProvider.getStaticHeaders()).thenReturn(givenStaticHeaders());

        AuthenticationData givenAuthenticationData =
                AuthenticationData.forAuthorization(givenClientId(), null, null, null, null, null);

        // when
        HttpRequest results = tested.prepareRequest(givenAuthenticationData);

        // then
        assertThat(results.getBody()).isNull();
        assertThat(results.getUrl()).isEqualTo(expectedUrl());
        assertThat(results.getMethod()).isEqualTo(GET);
        assertThat(results.getHeaders())
                .isNotEmpty()
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_JSON)),
                        entry(
                                ACCEPT,
                                singletonList(
                                        "text/javascript, text/html, "
                                                + "application/xml, text/xml, */*")));
    }

    private URL expectedUrl() {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", givenClientId());
        params.put("redirect_uri", "https://mfpredirecturi");
        params.put("response_type", "code");
        params.put("scope", "RegisteredClient UCAuthenticatedUser");

        URL url = new URL(givenBaseUrl() + "/az/v1/authorization");
        return url.queryParams(params);
    }

    @Test
    public void parseResponseShouldReturnCodeForValidResponse() throws URISyntaxException {
        // given
        String givenLocation = "https://mfpredirecturi?code=" + givenCode();
        int givenStatus = 200;

        HttpResponse givenResponse = mock(HttpResponse.class);
        when(givenResponse.getLocation()).thenReturn(new URI(givenLocation));
        when(givenResponse.getStatus()).thenReturn(givenStatus);

        // when
        ExternalApiCallResult<String> result = tested.parseResponse(givenResponse);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(givenCode());
    }
}
