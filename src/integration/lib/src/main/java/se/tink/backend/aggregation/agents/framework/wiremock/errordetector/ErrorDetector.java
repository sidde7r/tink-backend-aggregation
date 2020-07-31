package se.tink.backend.aggregation.agents.framework.wiremock.errordetector;

import com.github.tomakehurst.wiremock.http.MultiValue;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.ComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types.BodyEntity;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types.BodyEntityFactory;

public class ErrorDetector {

    private static final String CONTENT_TYPE_HEADER_KEY = "content-type";

    private Map<String, String> parseHeadersInGivenRequest(LoggedRequest givenRequest) {
        Map<String, String> headersInGivenRequest = new HashMap<>();
        givenRequest
                .getHeaders()
                .keys()
                .forEach(
                        key ->
                                headersInGivenRequest.put(
                                        key.toLowerCase(), givenRequest.getHeader(key)));
        return headersInGivenRequest;
    }

    private Map<String, MultiValuePattern> parseHeadersInExpectedRequest(
            RequestPattern expectedRequest) {

        Map<String, MultiValuePattern> headersInExpectedRequest = new HashMap<>();
        expectedRequest
                .getHeaders()
                .keySet()
                .forEach(
                        key ->
                                headersInExpectedRequest.put(
                                        key.toLowerCase(), expectedRequest.getHeaders().get(key)));
        return headersInExpectedRequest;
    }

    private Set<String> findCommonElements(Set<String> collection1, Set<String> collection2) {
        Set<String> result = new HashSet<>(collection1);
        result.retainAll(collection2);
        return result;
    }

    private void checkHeadersAndUpdateBuilder(
            LoggedRequest givenRequest,
            RequestPattern expectedRequest,
            CompareEntity.Builder builder) {
        Map<String, String> headersInGivenRequest = parseHeadersInGivenRequest(givenRequest);
        Map<String, MultiValuePattern> headersInExpectedRequest =
                parseHeadersInExpectedRequest(expectedRequest);

        Sets.difference(headersInExpectedRequest.keySet(), headersInGivenRequest.keySet())
                .forEach(builder::addMissingHeaderKeyInGivenRequest);

        Set<String> commonHeaders =
                findCommonElements(
                        headersInExpectedRequest.keySet(), headersInGivenRequest.keySet());

        commonHeaders.forEach(
                key -> {
                    String headerValueForGivenRequest = headersInGivenRequest.get(key);
                    MatchResult compareResultForHeaderValues =
                            headersInExpectedRequest
                                    .get(key)
                                    .match(
                                            new MultiValue(
                                                    key,
                                                    Collections.singletonList(
                                                            headerValueForGivenRequest)));

                    if (!compareResultForHeaderValues.isExactMatch()) {
                        builder.addHeaderKeyWithDifferentValue(key);
                    }
                });
    }

    private void checkRequestBodiesAndUpdateBuilder(
            LoggedRequest givenRequest,
            RequestPattern expectedRequest,
            CompareEntity.Builder builder)
            throws IOException {
        Map<String, String> headersInGivenRequest = parseHeadersInGivenRequest(givenRequest);
        MediaType mediaTypeOfGivenRequest =
                headersInGivenRequest.containsKey(CONTENT_TYPE_HEADER_KEY)
                        ? MediaType.valueOf(headersInGivenRequest.get(CONTENT_TYPE_HEADER_KEY))
                        : null;

        BodyEntity givenBodyEntity =
                BodyEntityFactory.create(givenRequest.getBodyAsString(), mediaTypeOfGivenRequest);

        String body = null;
        if (expectedRequest.getBodyPatterns() != null) {
            body =
                    expectedRequest.getBodyPatterns().stream()
                            .map(e -> e.getExpected().trim())
                            .collect(Collectors.joining("&"));
        }

        Map<String, MultiValuePattern> headersInExpectedRequest =
                parseHeadersInExpectedRequest(expectedRequest);
        MediaType mediaTypeOfExpectedRequest =
                headersInExpectedRequest.containsKey(CONTENT_TYPE_HEADER_KEY)
                        ? MediaType.valueOf(
                                headersInExpectedRequest.get(CONTENT_TYPE_HEADER_KEY).getExpected())
                        : null;

        BodyEntity expectedBodyEntity = BodyEntityFactory.create(body, mediaTypeOfExpectedRequest);
        ComparisonReporter reporter = expectedBodyEntity.compare(givenBodyEntity);
        builder.addBodyComparisonReporter(reporter);
    }

    public CompareEntity compare(LoggedRequest givenRequest, RequestPattern expectedRequest)
            throws IOException {

        CompareEntity.Builder builder =
                new CompareEntity.Builder(givenRequest.toString(), expectedRequest.toString())
                        .setUrlsMatching(
                                expectedRequest
                                        .getUrlMatcher()
                                        .match(givenRequest.getUrl())
                                        .isExactMatch())
                        .setHTTPMethodMatching(
                                givenRequest.getMethod().equals(expectedRequest.getMethod()));

        checkHeadersAndUpdateBuilder(givenRequest, expectedRequest, builder);
        checkRequestBodiesAndUpdateBuilder(givenRequest, expectedRequest, builder);
        return builder.build();
    }
}
