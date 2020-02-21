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
                            requestStartIndices.get(currentPairIndex) + 1,
                            responseStartIndices.get(currentPairIndex));
            HTTPRequest request = parseRequest(requestLines);
            int responseDataEndLine =
                    (currentPairIndex + 1) == requestStartIndices.size()
                            ? lines.size()
                            : requestStartIndices.get(currentPairIndex + 1);
            List<String> responseLines =
                    lines.subList(
                            responseStartIndices.get(currentPairIndex) + 1, responseDataEndLine);
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

    private HTTPRequest parseRequest(List<String> requestLines) {

        String requestMethod = parseRequestMethod(requestLines);
        String requestURL = removeHost(parseRequestURL(requestLines));
        List<Pair<String, String>> requestHeaders = parseHeaders(requestLines);
        Optional<String> requestBody = parseBody(requestLines);
        return requestBody
                .map(body -> new HTTPRequest(requestMethod, requestURL, requestHeaders, body))
                .orElse(new HTTPRequest(requestMethod, requestURL, requestHeaders));
    }

    private HTTPResponse parseResponse(List<String> responseLines) {

        Integer statusCode = parseStatusCode(responseLines);
        List<Pair<String, String>> responseHeaders = parseHeaders(responseLines);
        Optional<String> responseBody = parseBody(responseLines);
        return responseBody
                .map(body -> new HTTPResponse(responseHeaders, statusCode, body))
                .orElse(new HTTPResponse(responseHeaders, statusCode));
    }

    private List<Pair<String, String>> parseHeaders(List<String> rawData) {
        /*
         * Starting from second line, we have response headers. Headers continue until empty line
         */
        int firstEmptyLineIndex = rawData.indexOf("");
        return rawData.subList(1, firstEmptyLineIndex).stream()
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
         * Starting from second line, we check where is the first empty line, after that if there is
         * a line which is not empty, it must be the body
         */
        int firstEmptyLineIndex = rawData.indexOf("");
        return rawData.subList(firstEmptyLineIndex, rawData.size()).stream()
                .filter(line -> line.trim().length() > 0)
                .findFirst();
    }

    private String parseRequestMethod(List<String> rawData) {
        return rawData.get(0).trim().split(" ")[0];
    }

    private String parseRequestURL(List<String> rawData) {
        return rawData.get(0).trim().split(" ")[1];
    }

    private Integer parseStatusCode(List<String> rawData) {
        return new Integer(rawData.get(0).trim());
    }

    private String removeHost(final String url) {
        return url.replaceFirst(HTTP_HOST_REGEX, "");
    }
}
