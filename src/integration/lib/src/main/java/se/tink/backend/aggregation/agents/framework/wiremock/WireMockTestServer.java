package se.tink.backend.aggregation.agents.framework.wiremock;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPResponse;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.CompareEntity;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.ErrorDetector;
import se.tink.backend.aggregation.agents.framework.wiremock.parsing.BodyParserImpl;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.RequestResponseParser;
import se.tink.libraries.pair.Pair;

public class WireMockTestServer {

    private final WireMockServer wireMockServer;

    public WireMockTestServer(
            ImmutableSet<RequestResponseParser> parsers, boolean wireMockServerLogsEnabled) {

        WireMockConfiguration config = wireMockConfig().dynamicPort().dynamicHttpsPort();

        if (!wireMockServerLogsEnabled) {
            config.notifier(null);
        }

        wireMockServer = new WireMockServer(config);
        wireMockServer.start();
        Map<HTTPRequest, HTTPResponse> registeredPairs = new HashMap<>();
        parsers.forEach(
                parser ->
                        registerRequestResponsePairs(
                                parser.parseRequestResponsePairs(), registeredPairs));
    }

    public WireMockTestServer(ImmutableSet<RequestResponseParser> parsers) {

        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort());
        wireMockServer.start();
        Map<HTTPRequest, HTTPResponse> registeredPairs = new HashMap<>();
        parsers.forEach(
                parser ->
                        registerRequestResponsePairs(
                                parser.parseRequestResponsePairs(), registeredPairs));
    }

    public int getHttpsPort() {
        return wireMockServer.httpsPort();
    }

    public int getHttpPort() {
        return wireMockServer.port();
    }

    public void shutdown() {
        wireMockServer.shutdown();
    }

    public void resetRequests() {
        wireMockServer.resetRequests();
    }

    public boolean hadEncounteredAnError() {
        return !wireMockServer.findUnmatchedRequests().getRequests().isEmpty();
    }

    public CompareEntity findDifferencesBetweenFailedRequestAndItsClosestMatch()
            throws IOException {
        LoggedRequest failedRequest = wireMockServer.findUnmatchedRequests().getRequests().get(0);
        RequestPattern closestMatch =
                wireMockServer
                        .findTopNearMissesFor(failedRequest)
                        .getNearMisses()
                        .get(0)
                        .getStubMapping()
                        .getRequest();

        return new ErrorDetector().compare(failedRequest, closestMatch);
    }

    public String createErrorLogForFailedRequest() throws IOException {

        CompareEntity entity = findDifferencesBetweenFailedRequestAndItsClosestMatch();

        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(
                "The following request could not be matched with any expected requests\n");
        errorMessage.append(entity.getGivenRequest()).append("\n");
        errorMessage.append("The closest expected request is the following\n");
        errorMessage.append(entity.getExpectedRequest()).append("\n");
        errorMessage.append("The difference between them are the following\n");

        // Check URL
        if (!entity.areUrlsMatching()) {
            errorMessage.append(
                    "The URLs between the request and its closest match are different\n");
        }

        // Check HTTP methods
        if (!entity.areMethodsMatching()) {
            errorMessage.append("The HTTP methods are different\n");
        }

        // Check Headers
        if (entity.getMissingHeaderKeysInGivenRequest().size() > 0) {
            errorMessage.append("The following headers are missing in the request\n");
            entity.getMissingHeaderKeysInGivenRequest()
                    .forEach(key -> errorMessage.append(key).append("\n"));
        }

        entity.getHeaderKeysWithDifferentValues()
                .forEach(
                        key ->
                                errorMessage
                                        .append("The header ")
                                        .append(key)
                                        .append(
                                                " has different values for the request and its closest match\n"));

        // Check Query Parameters
        if (entity.getMissingQueryParametersInGivenRequest().size() > 0) {
            errorMessage.append("The following query parameters are missing in the request\n");
            entity.getMissingQueryParametersInGivenRequest()
                    .forEach(key -> errorMessage.append(key).append("\n"));
        }

        entity.getQueryParametersWithDifferentValues()
                .forEach(
                        key ->
                                errorMessage
                                        .append("The query parameter ")
                                        .append(key)
                                        .append(
                                                " has different values for the request and its closest match\n"));

        errorMessage.append(entity.getBodyComparisonReporter().report());
        return errorMessage.toString();
    }

    private void registerRequestResponsePairs(
            Set<Pair<HTTPRequest, HTTPResponse>> pairs,
            Map<HTTPRequest, HTTPResponse> registeredPairs) {
        // By default, WireMock will use the most recently added matching stub to satisfy the
        // request. To prevent mismatches when several requests have the same URL but different
        // subsets of parameters, headers and/or state, more specific requests should be added after
        // the more general ones.
        final List<Pair<HTTPRequest, HTTPResponse>> sortedPairs =
                pairs.stream()
                        .sorted(
                                Comparator.comparingInt(
                                                // Expected state after no state
                                                (Pair<HTTPRequest, HTTPResponse> p) ->
                                                        p.first.getExpectedState().isPresent()
                                                                ? 1
                                                                : 0)
                                        .thenComparingInt(
                                                // More parameters after fewer parameters
                                                (Pair<HTTPRequest, HTTPResponse> p) ->
                                                        p.first.getQuery().size())
                                        .thenComparingInt(
                                                // More headers after fewer headers
                                                (Pair<HTTPRequest, HTTPResponse> p) ->
                                                        p.first.getRequestHeaders().size()))
                        .collect(Collectors.toList());

        for (Pair<HTTPRequest, HTTPResponse> pair : sortedPairs) {

            final HTTPRequest request = pair.first;
            final HTTPResponse response = pair.second;

            // Check if this request has already been registered
            if (registeredPairs.containsKey(request)
                    && !registeredPairs.get(request).equals(response)) {
                throw new RuntimeException(
                        request.getPath()
                                + " - following request was already registered with a different response.\n"
                                + "If you need to have duplicate - make sure that response headers and bodies match (check number of empty lines at the end of response!).\n"
                                + "Otherwise - remove one of the conflicting pairs");
            }

            registeredPairs.put(request, response);

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
            wireMockServer.stubFor(builder);
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

        if ("put".equalsIgnoreCase(method)) {
            return WireMock.put(urlPattern);
        }

        if ("patch".equalsIgnoreCase(method)) {
            return WireMock.patch(urlPattern);
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

        new BodyParserImpl()
                .getStringValuePatterns(requestBody.get(), contentType)
                .forEach(builder::withRequestBody);
    }
}
