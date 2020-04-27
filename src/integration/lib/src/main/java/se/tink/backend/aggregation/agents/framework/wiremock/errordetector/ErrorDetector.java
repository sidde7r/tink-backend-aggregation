package se.tink.backend.aggregation.agents.framework.wiremock.errordetector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.BodyEntity.BodyType;
import se.tink.backend.aggregation.comparor.Comparor;
import se.tink.backend.aggregation.comparor.DifferenceCounter;
import se.tink.backend.aggregation.comparor.DifferenceEntity;
import se.tink.backend.aggregation.comparor.MapDifferenceEntity;

public class ErrorDetector {

    private final Comparor comparor =
            new Comparor(
                    new DifferenceCounter() {
                        @Override
                        public int numberOfDifferences(MapDifferenceEntity allDifferences) {
                            return allDifferences.getEntriesOnlyOnExpected().size()
                                    + allDifferences.getDifferenceInCommonKeys().size();
                        }
                    });

    private final ObjectMapper mapper = new ObjectMapper();
    private final XmlParser xmlParser = new XmlParser();

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

    private BodyEntity parseRequestBody(String requestBodySerialized, MediaType mediaType)
            throws JsonProcessingException {
        if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
            if (requestBodySerialized.startsWith("[")) {
                return new BodyEntity(BodyType.LIST, requestBodySerialized);
            }
            return new BodyEntity(BodyType.MAP, requestBodySerialized);
        }

        if (mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE)) {
            Map<String, Object> requestBody = new HashMap<>();

            Arrays.asList(requestBodySerialized.split("&"))
                    .forEach(
                            element -> {
                                String key = element.split("=")[0];
                                String value = element.split("=")[1];
                                requestBody.put(key, value);
                            });

            return new BodyEntity(BodyType.MAP, mapper.writeValueAsString(requestBody));
        }

        if (mediaType.equals(MediaType.TEXT_XML_TYPE)) {
            return new BodyEntity(BodyType.TEXT, xmlParser.normalizeXmlData(requestBodySerialized));
        }

        if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
            return new BodyEntity(BodyType.TEXT, requestBodySerialized);
        }

        throw new IllegalStateException("Could not parse request body");
    }

    private BodyEntity parseRequestBodyInGivenRequest(
            LoggedRequest givenRequest, MediaType mediaType) throws JsonProcessingException {

        if (Strings.isNullOrEmpty(givenRequest.getBodyAsString())) {
            return BodyEntity.emptyBody();
        }

        return parseRequestBody(givenRequest.getBodyAsString().trim(), mediaType);
    }

    private BodyEntity parseRequestBodyInExpectedRequest(
            RequestPattern expectedRequest, MediaType mediaType) throws JsonProcessingException {

        if (expectedRequest.getBodyPatterns() == null
                || expectedRequest.getBodyPatterns().size() == 0) {
            return BodyEntity.emptyBody();
        }

        if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
            return parseRequestBody(
                    expectedRequest.getBodyPatterns().get(0).getExpected().trim(), mediaType);
        } else if (mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE)) {
            String requestBodySerialized =
                    expectedRequest.getBodyPatterns().stream()
                            .map(e -> e.getExpected().trim())
                            .collect(Collectors.joining("&"));
            return parseRequestBody(requestBodySerialized, mediaType);
        } else if (mediaType.equals(MediaType.TEXT_XML_TYPE)) {
            return parseRequestBody(
                    expectedRequest.getBodyPatterns().get(0).getExpected().trim(), mediaType);
        } else if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
            return parseRequestBody(
                    expectedRequest.getBodyPatterns().get(0).getExpected().trim(), mediaType);
        }

        throw new IllegalStateException("Could not handle request body");
    }

    private Set<String> findCommonElements(Set<String> collection1, Set<String> collection2) {
        Set<String> result = new HashSet<>(collection1);
        result.retainAll(collection2);
        return result;
    }

    public void checkHeadersAndUpdateBuilder(
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

    public void checkRequestBodiesAndUpdateBuilder(
            LoggedRequest givenRequest,
            RequestPattern expectedRequest,
            CompareEntity.Builder builder)
            throws IOException {

        Map<String, String> headers = parseHeadersInGivenRequest(givenRequest);
        MediaType mediaType =
                headers.containsKey("content-type")
                        ? MediaType.valueOf(headers.get("content-type"))
                        : null;
        BodyEntity givenBodyEntity = parseRequestBodyInGivenRequest(givenRequest, mediaType);
        BodyEntity expectedBodyEntity =
                parseRequestBodyInExpectedRequest(expectedRequest, mediaType);

        boolean areBodyTypesMatching =
                givenBodyEntity.getBodyType().equals(expectedBodyEntity.getBodyType());
        builder.setBodyTypesMatching(areBodyTypesMatching);

        if (areBodyTypesMatching && !expectedBodyEntity.getBodyType().equals(BodyType.EMPTY)) {

            DifferenceEntity difference = null;

            if (expectedBodyEntity.getBodyType().equals(BodyType.MAP)) {
                Map<String, Object> expectedBody = BodyEntity.getMap(expectedBodyEntity);
                Map<String, Object> failedBody = BodyEntity.getMap(givenBodyEntity);
                difference = comparor.findDifferencesInMappings(expectedBody, failedBody);
            }

            if (expectedBodyEntity.getBodyType().equals(BodyType.LIST)) {
                List<?> expectedBody = BodyEntity.getList(expectedBodyEntity);
                List<?> failedBody = BodyEntity.getList(givenBodyEntity);
                difference = comparor.areListsMatching(expectedBody, failedBody);
            }

            if (expectedBodyEntity.getBodyType().equals(BodyType.TEXT)) {
                String expectedBody = BodyEntity.getText(expectedBodyEntity);
                String failedBody = BodyEntity.getText(givenBodyEntity);
                if (!expectedBody.equals(failedBody)) {
                    builder.addBodyKeyWithDifferentValue("Request bodies are different");
                }
            }

            if (difference instanceof MapDifferenceEntity) {
                MapDifferenceEntity diff = (MapDifferenceEntity) difference;

                diff.getEntriesOnlyOnExpected()
                        .keySet()
                        .forEach(builder::addMissingBodyKeyInGivenRequest);

                diff.getDifferenceInCommonKeys()
                        .keySet()
                        .forEach(builder::addBodyKeyWithDifferentValue);
            }
        }
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
