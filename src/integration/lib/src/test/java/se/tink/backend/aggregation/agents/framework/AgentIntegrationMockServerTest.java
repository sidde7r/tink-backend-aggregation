package se.tink.backend.aggregation.agents.framework;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Rule;
import se.tink.libraries.pair.Pair;

public class AgentIntegrationMockServerTest {

    @Rule public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().httpsPort(9999));

    public void prepareMockServer(String mainPath, String mockResource, String host) {
        // TODO: Reset stub here
        // wireMockRule.reset();
        String fileContent = readFile(String.format("%s/%s", mainPath, mockResource));
        List<Pair<HTTPRequest, HTTPResponse>> data = parseFileContent(fileContent, host);
        buildMockServer(data);
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

        String[] firstLine =
                requestLines.get(0).substring(requestHeaderPrefix.length()).trim().split(" ");

        String requestMethod = firstLine[0];
        String requestURL = firstLine[1].replace(host, "");

        List<String> requestHeaders =
                requestLines.subList(1, requestLines.size()).stream()
                        .filter(line -> line.startsWith(requestHeaderPrefix))
                        .map(line -> line.substring(requestHeaderPrefix.length()).trim())
                        .filter(line -> line.trim().length() > 0)
                        .collect(Collectors.toList());

        Optional<String> requestBody =
                requestLines.subList(1, requestLines.size()).stream()
                        .filter(line -> !line.startsWith(requestHeaderPrefix))
                        .filter(line -> line.trim().length() > 0)
                        .findFirst();

        return new HTTPRequest(requestMethod, requestURL, requestHeaders, requestBody);
    }

    private HTTPResponse parseResponse(List<String> responseLines, String responseHeaderPrefix) {
        Integer responseCode =
                new Integer(responseLines.get(0).substring(responseHeaderPrefix.length()).trim());

        List<String> responseHeaders =
                responseLines.subList(1, responseLines.size()).stream()
                        .filter(line -> line.startsWith(responseHeaderPrefix))
                        .map(line -> line.substring(responseHeaderPrefix.length()).trim())
                        .filter(line -> line.trim().length() > 0)
                        .collect(Collectors.toList());

        Optional<String> responseBody =
                responseLines.subList(1, responseLines.size()).stream()
                        .filter(line -> !line.startsWith(responseHeaderPrefix))
                        .filter(line -> line.trim().length() > 0)
                        .findFirst();

        return new HTTPResponse(responseHeaders, responseBody, responseCode);
    }

    private List<Pair<HTTPRequest, HTTPResponse>> parseFileContent(
            String fileContent, String host) {

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

            HTTPRequest request = parseRequest(requestLines, requestHeaderPrefix, host);

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

    private Pair<String, String> parseHeader(String header) {
        String key = header.substring(0, header.lastIndexOf(":")).trim();
        String value = header.substring(header.lastIndexOf(":") + 1).trim();
        return new Pair<>(key, value);
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
                    .map(header -> parseHeader(header))
                    .forEach(header -> builder.withHeader(header.first, equalTo(header.second)));

            request.getRequestBody()
                    .ifPresent(body -> builder.withRequestBody(equalToJson(body, true, true)));

            ResponseDefinitionBuilder res = aResponse();

            response.getResponseHeaders().stream()
                    .map(header -> parseHeader(header))
                    .forEach(header -> res.withHeader(header.first, header.second));

            res.withStatus(response.getStatusCode());

            response.getResponseBody().ifPresent(body -> res.withBody(body));

            builder.willReturn(res);
            wireMockRule.stubFor(builder);
        }
    }
}
