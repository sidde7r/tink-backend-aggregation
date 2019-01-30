package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.Client;
import java.util.Map;
import org.assertj.core.util.Strings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JerseyClientWrapperTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    private int mockServerPort;

    @Before
    public void setUp() {
        mockServerPort = wireMockRule.port();
    }

    @Test
    public void addFilter() {
        ClientFilterFactory filterFactoryMock = mock(ClientFilterFactory.class);
        Client mockClient = mock(Client.class);
        JerseyClientWrapper jerseyClientWrapper = new JerseyClientWrapper(mockClient, mock(ApiConfiguration.class));

        jerseyClientWrapper.attachHttpFilters(filterFactoryMock);

        verify(filterFactoryMock, times(1)).addClientFilter(mockClient);
    }

    @Test
    public void postIncludesBodyAndUrl() {
        // Hook the wiremock server to URI and match the headers + content
        stubFor(post(urlEqualTo("/test"))
                .withHeader("Content-Type", matching("application/json; charset=utf-8"))
                .withHeader("Accept", matching("application/json"))
                .withRequestBody(matching("\\{\\\"input\\\":\\\"value\\\"\\}"))
                .willReturn(aResponse()
                        .withBody("...response...")));

        // Setup
        JerseyClientWrapper jerseyClientWrapper = new JerseyClientWrapper(new ClientStub(),
                new ConfigurationStub(mockServerPort));

        // Post the stuff, which should generated simulated response from wiremock
        String response = jerseyClientWrapper.post(new RequestStub(), String.class);

        assertThat(response).isEqualTo("...response...");
    }

    @Test
    public void getExcludesBodyButResolvesUrl() {
        // Hook the wiremock server to URI and match the headers + content
        stubFor(get(urlEqualTo("/test"))
                .withHeader("Accept", matching("application/json"))
                .withRequestBody(matching("^$")) // Empty body
                .willReturn(aResponse()
                        .withBody("...response...")));

        // Setup
        JerseyClientWrapper jerseyClientWrapper = new JerseyClientWrapper(new ClientStub(),
                new ConfigurationStub(mockServerPort));

        // Post the stuff, which should generated simulated response from wiremock
        String response = jerseyClientWrapper.get(new RequestStub(), String.class);

        assertThat(response).isEqualTo("...response...");
    }

    @Test
    public void addsHeadersToRequests() {
        // Hook the wiremock server to URI and match the headers + content
        stubFor(get(urlEqualTo("/test"))
                .withHeader("Accept", matching("application/json"))
                .withHeader("Custom-Header", matching("my-custom"))
                .withRequestBody(matching("^$")) // Empty body
                .willReturn(aResponse()
                        .withBody("...response...")));

        // Setup
        JerseyClientWrapper jerseyClientWrapper = new JerseyClientWrapper(
                new ClientStub(), new ConfigurationStub("Custom-Header", "my-custom", mockServerPort));

        // Post the stuff, which should generated simulated response from wiremock
        String response = jerseyClientWrapper.get(new RequestStub(), String.class);

        // We should only get here if the header `Custom-Header` was set and matched by the stubFor…
        assertThat(response).isEqualTo("...response...");
    }

    private static class ClientStub extends Client {
    }

    private static class ConfigurationStub implements ApiConfiguration {
        private final String customHeaderKey;
        private final String customHeaderValue;
        private final int port;

        public ConfigurationStub(int port) {
            this(null, null, port);
        }

        public ConfigurationStub(String customHeaderKey, String customHeaderValue, int port) {
            this.customHeaderKey = customHeaderKey;
            this.customHeaderValue = customHeaderValue;
            this.port = port;
        }

        @Override
        public String getBaseUrl() {
            return "localhost:" + port;
        }

        @Override
        public boolean isHttps() {
            return false;
        }

        @Override
        public Map<String, String> getHeaders() {
            if (Strings.isNullOrEmpty(customHeaderKey)) {
                return ImmutableMap.of();
            }

            return ImmutableMap.of(customHeaderKey, customHeaderValue);
        }
    }

    private static class RequestStub implements ApiRequest {
        public String input = "value";

        @JsonIgnore
        @Override
        public String getUriPath() {
            return "/test";
        }
    }

}
