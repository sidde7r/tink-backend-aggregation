package se.tink.backend.aggregation.agents.framework.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.List;
import org.junit.Rule;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPResponse;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.WiremockRequestResponseParser;
import se.tink.libraries.pair.Pair;

public class AgentIntegrationMockServerTest {

    @Rule
    public WireMockRule wireMockRule =
            new WireMockRule(WireMockConfiguration.options().dynamicHttpsPort());

    private WiremockRequestResponseParser parser;

    public int getWireMockPort() {
        return wireMockRule.httpsPort();
    }

    public void prepareMockServer(WiremockRequestResponseParser parser) {
        // TODO: Reset stub here
        // wireMockRule.reset();
        List<Pair<HTTPRequest, HTTPResponse>> data = parser.parseRequestResponsePairs();
        buildMockServer(data);
    }

    private void buildMockServer(List<Pair<HTTPRequest, HTTPResponse>> pairs) {
        for (Pair<HTTPRequest, HTTPResponse> pair : pairs) {
            HTTPRequest request = pair.first;
            HTTPResponse response = pair.second;
            MappingBuilder builder;
            if (request.getMethod().equalsIgnoreCase("get")) {
                builder = get(urlPathEqualTo(request.getUrl()));
            } else if (request.getMethod().equalsIgnoreCase("post")) {
                builder = post(urlPathEqualTo(request.getUrl()));
            } else {
                throw new RuntimeException(
                        "The following HTTP method cannot be handled by the test framework : "
                                + request.getMethod());
            }
            request.getRequestHeaders().stream()
                    .forEach(header -> builder.withHeader(header.first, equalTo(header.second)));
            request.getRequestBody()
                    .ifPresent(body -> builder.withRequestBody(equalToJson(body, true, true)));
            ResponseDefinitionBuilder res = aResponse();
            response.getResponseHeaders().stream()
                    .forEach(header -> res.withHeader(header.first, header.second));
            res.withStatus(response.getStatusCode());
            response.getResponseBody().ifPresent(body -> res.withBody(body));
            builder.willReturn(res);
            wireMockRule.stubFor(builder);
        }
    }
}
