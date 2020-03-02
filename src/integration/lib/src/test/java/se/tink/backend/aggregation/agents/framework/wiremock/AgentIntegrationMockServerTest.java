package se.tink.backend.aggregation.agents.framework.wiremock;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import java.util.List;
import java.util.Optional;
import org.junit.Rule;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPResponse;
import se.tink.backend.aggregation.agents.framework.wiremock.parsing.BodyParser;
import se.tink.backend.aggregation.agents.framework.wiremock.parsing.BodyParserImpl;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.RequestResponseParser;
import se.tink.libraries.pair.Pair;

public class AgentIntegrationMockServerTest {

    @Rule
    public WireMockRule wireMockRule =
            new WireMockRule(WireMockConfiguration.options().dynamicPort().dynamicHttpsPort());

    private BodyParser bodyParser;

    protected int getWireMockPort() {
        return wireMockRule.httpsPort();
    }

    protected int getPort() {
        return wireMockRule.port();
    }

    protected void prepareMockServer(RequestResponseParser parser) {
        // TODO: Reset stub here
        // wireMockRule.reset();
        this.bodyParser = new BodyParserImpl();
        List<Pair<HTTPRequest, HTTPResponse>> data = parser.parseRequestResponsePairs();
        buildMockServer(data);
    }

    private void buildMockServer(List<Pair<HTTPRequest, HTTPResponse>> pairs) {
        for (Pair<HTTPRequest, HTTPResponse> pair : pairs) {

            final HTTPRequest request = pair.first;
            final HTTPResponse response = pair.second;

            final MappingBuilder builder = parseRequestType(request);

            request.getExpectedState()
                    .ifPresent(state -> builder.inScenario("test").whenScenarioStateIs(state));
            request.getQuery()
                    .forEach(
                            queryParam ->
                                    builder.withQueryParam(
                                            queryParam.getName(),
                                            WireMock.equalTo(queryParam.getValue())));
            parseRequestHeaders(request, builder);
            parseRequestBody(request, builder);

            ResponseDefinitionBuilder res = WireMock.aResponse();
            response.getResponseHeaders()
                    .forEach(header -> res.withHeader(header.first, header.second));
            res.withStatus(response.getStatusCode());
            response.getResponseBody().ifPresent(res::withBody);
            builder.willReturn(res);
            response.getToState()
                    .ifPresent(state -> builder.inScenario("test").willSetStateTo(state));
            wireMockRule.stubFor(builder);
        }
    }

    private MappingBuilder parseRequestType(final HTTPRequest request) {

        final String method = request.getMethod();
        final UrlPathPattern urlPattern = WireMock.urlPathEqualTo(request.getPath());

        if ("get".equalsIgnoreCase(method)) {
            return WireMock.get(urlPattern);
        }

        if ("post".equalsIgnoreCase(method)) {
            return WireMock.post(urlPattern);
        }

        throw new RuntimeException(
                "The following HTTP method cannot be handled by the test framework : " + method);
    }

    private void parseRequestHeaders(final HTTPRequest request, final MappingBuilder builder) {

        request.getRequestHeaders()
                .forEach(
                        header ->
                                builder.withHeader(header.first, WireMock.equalTo(header.second)));
    }

    private void parseRequestBody(final HTTPRequest request, final MappingBuilder builder) {

        final Optional<String> requestBody = request.getRequestBody();
        if (!requestBody.isPresent()) {
            return; // No body, no parsing needed.
        }

        final String contentType =
                request.getContentType()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find or parse Content-Type header in requests when reading mock file. To use WireMock test server each of the requests coming from the agents needs to specify the correct Content-Type."));

        bodyParser
                .getStringValuePatterns(requestBody.get(), contentType)
                .forEach(builder::withRequestBody);
    }
}
