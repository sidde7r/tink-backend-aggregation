package se.tink.backend.aggregation.agents.framework.wiremock.utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPResponse;
import se.tink.libraries.pair.Pair;

public class AapFileParser implements RequestResponseParser {

    // Matches everything up until the third '/' in url.
    private static final String HTTP_HOST_REGEX = "^.*?//[^/]+";

    private final List<String> lines;

    public AapFileParser(String rawFileContent) {
        this.lines =
                new ArrayList<>(Arrays.asList(rawFileContent.split("\n")))
                        .stream()
                                .filter(line -> !line.startsWith("#"))
                                .collect(Collectors.toList());
    }

    @Override
    public ImmutableSet<Pair<HTTPRequest, HTTPResponse>> parseRequestResponsePairs() {

        List<Integer> requestStartIndices =
                findLineIndicesStartingWithExpression(lines, "REQUEST ");
        List<Integer> responseStartIndices =
                findLineIndicesStartingWithExpression(lines, "RESPONSE ");
        Set<Pair<HTTPRequest, HTTPResponse>> pairs = new HashSet<>();
        int totalPairAmount = requestStartIndices.size();
        for (int currentPairIndex = 0; currentPairIndex < totalPairAmount; currentPairIndex++) {
            List<String> requestLines =
                    lines.subList(
                            requestStartIndices.get(currentPairIndex),
                            responseStartIndices.get(currentPairIndex));
            HTTPRequest request = parseRequest(requestLines);
            int responseDataEndLine =
                    (currentPairIndex + 1) == requestStartIndices.size()
                            ? lines.size()
                            : requestStartIndices.get(currentPairIndex + 1);
            List<String> responseLines =
                    lines.subList(responseStartIndices.get(currentPairIndex), responseDataEndLine);
            HTTPResponse response = parseResponse(responseLines);
            pairs.add(new Pair<>(request, response));
        }
        return ImmutableSet.copyOf(pairs);
    }

    private List<Integer> findLineIndicesStartingWithExpression(
            List<String> lines, String searchedExpression) {
        return lines.stream()
                .filter(line -> line.startsWith(searchedExpression))
                .map(lines::indexOf)
                .collect(Collectors.toList());
    }

    private HTTPRequest parseRequest(final List<String> requestLines) {

        Optional<String> expectedState = parseExpectedState(requestLines);
        String requestMethod = parseRequestMethod(requestLines);
        String requestURL = removeHost(parseRequestURL(requestLines));
        ImmutableSet<Pair<String, String>> requestHeaders = parseHeaders(requestLines);
        Optional<String> requestBody = parseBody(requestLines);

        HTTPRequest.Builder httpRequestBuilder =
                new HTTPRequest.Builder(requestMethod, requestURL, requestHeaders);
        requestBody.ifPresent(httpRequestBuilder::setRequestBody);
        expectedState.ifPresent(httpRequestBuilder::setExpectedState);
        return httpRequestBuilder.build();
    }

    private HTTPResponse parseResponse(List<String> responseLines) {

        Optional<String> toState = parseToState(responseLines);
        Integer statusCode = parseStatusCode(responseLines);
        ImmutableSet<Pair<String, String>> responseHeaders = parseHeaders(responseLines);
        Optional<String> responseBody = parseBody(responseLines);
        HTTPResponse.Builder httpResponseBuilder =
                new HTTPResponse.Builder(responseHeaders, statusCode);
        responseBody.ifPresent(httpResponseBuilder::setResponseBody);
        toState.ifPresent(httpResponseBuilder::setToState);
        return httpResponseBuilder.build();
    }

    private ImmutableSet<Pair<String, String>> parseHeaders(List<String> rawData) {
        /*
         * Starting from third line, we have response headers. Headers continue until empty line
         */
        int firstEmptyLineIndex = rawData.indexOf("");
        return rawData.subList(2, firstEmptyLineIndex).stream()
                .map(this::parseHeader)
                .collect(ImmutableSet.toImmutableSet());
    }

    private Pair<String, String> parseHeader(String header) {
        // AAP-381: Since Wiremock does not support HTTP2 pseudo headers
        // we convert them to regular headers
        if (header.startsWith(":")) {
            header = header.substring(1);
        }
        final int deliminatorIndex = header.indexOf(':');
        String key = header.substring(0, deliminatorIndex).trim();
        String value = header.substring(deliminatorIndex + 1).trim();
        return new Pair<>(key, value);
    }

    private Optional<String> parseBody(List<String> rawData) {
        /*
         * Starting from third line, we check where is the first empty line, after that,
         * everything is body, except the last line if it's empty.
         */
        final int firstEmptyLineIndex = rawData.indexOf("");
        final int lastLineIndex = rawData.size() - 1;
        final int afterBodyLineIndex =
                firstEmptyLineIndex < lastLineIndex && rawData.get(lastLineIndex).isEmpty()
                        ? lastLineIndex
                        : rawData.size();

        return Optional.ofNullable(
                Strings.emptyToNull(
                        rawData.subList(firstEmptyLineIndex + 1, afterBodyLineIndex).stream()
                                .collect(Collectors.joining("\n"))));
    }

    private String parseRequestMethod(List<String> rawData) {
        return rawData.get(1).trim().split(" ")[0];
    }

    private String parseRequestURL(List<String> rawData) {
        return rawData.get(1).trim().split(" ")[1];
    }

    private Integer parseStatusCode(List<String> rawData) {
        return Integer.parseInt(rawData.get(1).trim());
    }

    private Optional<String> parseToState(List<String> rawData) {
        String[] firstLineWords = rawData.get(0).split(" ");
        if (firstLineWords.length < 3) {
            return Optional.empty();
        }
        if (firstLineWords.length == 3) {
            throw new UnsupportedOperationException(
                    String.format(
                            "Invalid operation in line %s. An operation must have an argument",
                            rawData.get(0)));
        }
        if (!firstLineWords[2].equalsIgnoreCase("SET")) {
            throw new UnsupportedOperationException(
                    String.format(
                            "Invalid operation: %s in line %s. A response can only perform SET operation on state",
                            Arrays.toString(firstLineWords), rawData.get(0)));
        }
        return Optional.of(firstLineWords[3]);
    }

    private Optional<String> parseExpectedState(List<String> rawData) {
        String[] firstLineWords = rawData.get(0).split(" ");
        if (firstLineWords.length < 3) {
            return Optional.empty();
        }
        if (firstLineWords.length == 3) {
            throw new UnsupportedOperationException(
                    String.format(
                            "Invalid operation in line %s. An operation must have an argument",
                            rawData.get(0)));
        }
        if (!firstLineWords[2].equalsIgnoreCase("MATCH")) {
            throw new UnsupportedOperationException(
                    String.format(
                            "Invalid operation: %s in line %s. A response can only perform MATCH operation on state",
                            Arrays.toString(firstLineWords), rawData.get(0)));
        }
        return Optional.of(firstLineWords[3]);
    }

    private String removeHost(final String url) {
        return url.replaceFirst(HTTP_HOST_REGEX, "");
    }
}
