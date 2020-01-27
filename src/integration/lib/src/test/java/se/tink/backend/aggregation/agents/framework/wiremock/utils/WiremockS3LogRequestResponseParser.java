package se.tink.backend.aggregation.agents.framework.utils.wiremock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPResponse;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.WiremockRequestResponseParser;
import se.tink.libraries.pair.Pair;

public class WiremockS3LogRequestResponseParser implements WiremockRequestResponseParser {

    private final String fileContent;
    private final String apiHost;

    public WiremockS3LogRequestResponseParser(String s3FilePath, String apiHost) {
        this.fileContent = readFile(s3FilePath);
        this.apiHost = apiHost;
    }

    public List<Pair<HTTPRequest, HTTPResponse>> parseRequestResponsePairs() {

        List<String> lines = Arrays.asList(fileContent.split("\n"));

        List<Integer> requestStartIndices = findLineIndices(lines, "Client out-bound request");
        List<Integer> responseStartIndices = findLineIndices(lines, "Client in-bound response");

        List<Pair<HTTPRequest, HTTPResponse>> pairs = new ArrayList<>();
        int totalPairAmount = requestStartIndices.size();

        for (int currentPairIndex = 0; currentPairIndex < totalPairAmount; currentPairIndex++) {

            final String requestHeaderPrefix = new Integer(currentPairIndex + 1).toString() + " >";
            final String responseHeaderPrefix = new Integer(currentPairIndex + 1).toString() + " <";

            List<String> requestLines =
                    lines.subList(
                            requestStartIndices.get(currentPairIndex) + 2,
                            responseStartIndices.get(currentPairIndex));

            HTTPRequest request = parseRequest(requestLines, requestHeaderPrefix, apiHost);

            int responseDataEndLine =
                    (currentPairIndex + 1) == requestStartIndices.size()
                            ? lines.size()
                            : requestStartIndices.get(currentPairIndex + 1);

            List<String> responseLines =
                    lines.subList(
                            responseStartIndices.get(currentPairIndex) + 2, responseDataEndLine);

            HTTPResponse response = parseResponse(responseLines, responseHeaderPrefix);
            pairs.add(new Pair<>(request, response));
        }

        return pairs;
    }

    private String readFile(String filePath) {
        String fileContent;
        try {
            fileContent =
                    new String(
                            Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileContent;
    }

    /**
     * For a given List of String (lines), returns the list of indices where the line contains a
     * substring given as a parameter (subStr)
     */
    private List<Integer> findLineIndices(List<String> lines, String subStr) {
        return lines.stream()
                .filter(line -> line.contains(subStr))
                .map(line -> lines.indexOf(line))
                .collect(Collectors.toList());
    }

    private HTTPRequest parseRequest(
            List<String> requestLines, String requestHeaderPrefix, String host) {

        String requestMethod = parseRequestMethod(requestLines, requestHeaderPrefix);
        String requestURL = parseRequestURL(requestLines, requestHeaderPrefix).replace(host, "");
        List<Pair<String, String>> requestHeaders = parseHeaders(requestLines, requestHeaderPrefix);
        Optional<String> requestBody = parseBody(requestLines, requestHeaderPrefix);

        if (requestBody.isPresent()) {
            return new HTTPRequest(requestMethod, requestURL, requestHeaders, requestBody.get());
        } else {
            return new HTTPRequest(requestMethod, requestURL, requestHeaders);
        }
    }

    private HTTPResponse parseResponse(List<String> responseLines, String responseHeaderPrefix) {

        Integer statusCode = parseStatusCode(responseLines, responseHeaderPrefix);
        List<Pair<String, String>> responseHeaders =
                parseHeaders(responseLines, responseHeaderPrefix);
        Optional<String> responseBody = parseBody(responseLines, responseHeaderPrefix);

        if (responseBody.isPresent()) {
            return new HTTPResponse(responseHeaders, statusCode, responseBody.get());
        } else {
            return new HTTPResponse(responseHeaders, statusCode);
        }
    }

    private List<Pair<String, String>> parseHeaders(List<String> rawData, String redundantPrefix) {
        /** Starting from second line, we have response headers */
        return rawData.subList(1, rawData.size()).stream()
                .filter(line -> line.startsWith(redundantPrefix))
                .map(line -> line.substring(redundantPrefix.length()).trim())
                .filter(line -> line.trim().length() > 0)
                .map(header -> parseHeader(header))
                .collect(Collectors.toList());
    }

    private Pair<String, String> parseHeader(String header) {
        String key = header.substring(0, header.lastIndexOf(":")).trim();
        String value = header.substring(header.lastIndexOf(":") + 1).trim();
        return new Pair<>(key, value);
    }

    private Optional<String> parseBody(List<String> rawData, String redundantPrefix) {
        /**
         * Starting from second line, we check a line which does not contain "redundantPrefix" if
         * there is such line, it contains the body
         */
        return rawData.subList(1, rawData.size()).stream()
                .filter(line -> !line.startsWith(redundantPrefix))
                .filter(line -> line.trim().length() > 0)
                .findFirst();
    }

    private String parseRequestMethod(List<String> rawData, String redundantPrefix) {
        return rawData.get(0).substring(redundantPrefix.length()).trim().split(" ")[0];
    }

    private String parseRequestURL(List<String> rawData, String redundantPrefix) {
        return rawData.get(0).substring(redundantPrefix.length()).trim().split(" ")[1];
    }

    private Integer parseStatusCode(List<String> rawData, String redundantPrefix) {
        return new Integer(rawData.get(0).substring(redundantPrefix.length()).trim());
    }
}
