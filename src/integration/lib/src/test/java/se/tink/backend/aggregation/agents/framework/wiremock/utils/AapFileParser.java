package se.tink.backend.aggregation.agents.framework.wiremock.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPResponse;
import se.tink.libraries.pair.Pair;

public class AapFileParser implements RequestResponseParser {

    // Matches everything up until the third '/' in url.
    private static final String HTTP_HOST_REGEX = "^.*//[^/]+";

    private final List<String> lines;

    public AapFileParser(String rawFileContent) {
        this.lines =
                new ArrayList<>(Arrays.asList(rawFileContent.split("\n")))
                        .stream()
                                .filter(line -> !line.startsWith("#"))
                                .collect(Collectors.toList());
    }

    public AapFileParser(List<String> lines) {
        this.lines = lines;
    }

    @Override
    public List<Pair<HTTPRequest, HTTPResponse>> parseRequestResponsePairs() {

        List<Integer> requestStartIndices =
                findLineIndicesContainingGivenExpression(lines, "REQUEST");
        List<Integer> responseStartIndices =
                findLineIndicesContainingGivenExpression(lines, "RESPONSE");
        List<Pair<HTTPRequest, HTTPResponse>> pairs = new ArrayList<>();
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
        return pairs;
    }

    private List<Integer> findLineIndicesContainingGivenExpression(
            List<String> lines, String searchedExpression) {
        return lines.stream()
                .filter(line -> line.contains(searchedExpression))
                .map(line -> lines.indexOf(line))
                .collect(Collectors.toList());
    }

    private HTTPRequest parseRequest(final List<String> requestLines) {

        Optional<String> expectedState = parseExpectedState(requestLines);
        String requestMethod = parseRequestMethod(requestLines);
        String requestURL = removeHost(parseRequestURL(requestLines));
        List<Pair<String, String>> requestHeaders = parseHeaders(requestLines);
        Optional<String> requestBody = parseBody(requestLines);

        HTTPRequest.Builder httpRequestBuilder =
                new HTTPRequest.Builder(requestMethod, requestURL, requestHeaders);
        requestBody.ifPresent(body -> httpRequestBuilder.withRequestBody(body));
        expectedState.ifPresent(state -> httpRequestBuilder.withExpectedState(state));
        return httpRequestBuilder.build();
    }

    private HTTPResponse parseResponse(List<String> responseLines) {

        Optional<String> toState = parseToState(responseLines);
        Integer statusCode = parseStatusCode(responseLines);
        List<Pair<String, String>> responseHeaders = parseHeaders(responseLines);
        Optional<String> responseBody = parseBody(responseLines);
        HTTPResponse.Builder httpResponseBuilder =
                new HTTPResponse.Builder(responseHeaders, statusCode);
        responseBody.ifPresent(body -> httpResponseBuilder.withResponseBody(body));
        toState.ifPresent(state -> httpResponseBuilder.withToState(state));
        return httpResponseBuilder.build();
    }

    private List<Pair<String, String>> parseHeaders(List<String> rawData) {
        /*
         * Starting from third line, we have response headers. Headers continue until empty line
         */
        int firstEmptyLineIndex = rawData.indexOf("");
        return rawData.subList(2, firstEmptyLineIndex).stream()
                .map(this::parseHeader)
                .collect(Collectors.toList());
    }

    private Pair<String, String> parseHeader(String header) {
        final int deliminatorIndex = header.indexOf(":");
        String key = header.substring(0, deliminatorIndex).trim();
        String value = header.substring(deliminatorIndex + 1).trim();
        return new Pair<>(key, value);
    }

    private Optional<String> parseBody(List<String> rawData) {
        /*
         * Starting from third line, we check where is the first empty line, after that if there is
         * a line which is not empty, it must be the body
         */
        int firstEmptyLineIndex = rawData.indexOf("");
        return rawData.subList(firstEmptyLineIndex, rawData.size()).stream()
                .filter(line -> line.trim().length() > 0)
                .findFirst();
    }

    private String parseRequestMethod(List<String> rawData) {
        return rawData.get(1).trim().split(" ")[0];
    }

    private String parseRequestURL(List<String> rawData) {
        return rawData.get(1).trim().split(" ")[1];
    }

    private Integer parseStatusCode(List<String> rawData) {
        return new Integer(rawData.get(1).trim());
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
                            firstLineWords, rawData.get(0)));
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
                            firstLineWords, rawData.get(0)));
        }
        return Optional.of(firstLineWords[3]);
    }

    private String removeHost(final String url) {
        return url.replaceFirst(HTTP_HOST_REGEX, "");
    }
}
