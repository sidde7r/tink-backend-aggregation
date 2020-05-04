package se.tink.backend.aggregation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class SystemTestUtils {

    private static ObjectMapper mapper = new ObjectMapper();
    private static Logger log = LoggerFactory.getLogger(SystemTestUtils.class);

    private static Set<String> finalCredentialsStatus =
            ImmutableSet.of("TEMPORARY_ERROR", "AUTHENTICATION_ERROR", "UPDATED");

    public static ResponseEntity<String> makePostRequest(String url, Object requestBody)
            throws Exception {

        TestRestTemplate restTemplate = new TestRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Tink-App-Id", "00000000-0000-0000-0000-000000000000");
        headers.add("X-Tink-Client-Api-Key", "00000000-0000-0000-0000-000000000000");

        HttpEntity<Object> request = new HttpEntity<Object>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        int status = response.getStatusCodeValue();
        if (status >= 300) {
            throw new Exception(
                    "Invalid HTTP response status "
                            + "code "
                            + status
                            + " from web service server.");
        }

        return response;
    }

    public static ResponseEntity<String> makeGetRequest(String url, HttpHeaders headers)
            throws Exception {
        TestRestTemplate restTemplate = new TestRestTemplate();

        ResponseEntity<String> response =
                new TestRestTemplate()
                        .exchange(
                                url, HttpMethod.GET, new HttpEntity<Object>(headers), String.class);

        int status = response.getStatusCodeValue();
        if (status >= 300) {
            throw new Exception(
                    "Invalid HTTP response status "
                            + "code "
                            + status
                            + " from web service server.");
        }

        return response;
    }

    public static Optional<List<String>> fetchCallbacksForEndpoint(String url, String endPoint)
            throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");

        Map<String, List<String>> pushedData = new HashMap<>();

        ResponseEntity<String> dataResult = makeGetRequest(url, headers);

        pushedData = new ObjectMapper().readValue(dataResult.getBody(), Map.class);
        if (pushedData.keySet().size() == 0) {
            return Optional.empty();
        }

        if (!pushedData.containsKey(endPoint)) {
            return Optional.empty();
        }

        return Optional.of(pushedData.get(endPoint));
    }

    public static Optional<String> pollForFinalCredentialsUpdateStatusUntilFlowEnds(
            String url, int retryAmount, int sleepSeconds) throws Exception {

        for (int i = 0; i < retryAmount; i++) {
            Optional<List<String>> updateCredentialsCallback =
                    fetchCallbacksForEndpoint(url, "updateCredentials");

            if (!updateCredentialsCallback.isPresent()) {
                Uninterruptibles.sleepUninterruptibly(sleepSeconds, TimeUnit.SECONDS);
                continue;
            }

            List<String> credentialsUpdateCallbacks = updateCredentialsCallback.get();
            JsonNode latestCredentialsUpdateCallback =
                    mapper.readTree(
                            credentialsUpdateCallbacks.get(credentialsUpdateCallbacks.size() - 1));
            String credentialsStatus =
                    latestCredentialsUpdateCallback.get("credentials").get("status").asText();

            if (!finalCredentialsStatus.contains(credentialsStatus)) {
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                continue;
            }
            return Optional.of(credentialsStatus);
        }
        throw new RuntimeException("Timeout for polling attempt");
    }

    public static List<JsonNode> pollForAllCallbacksForAnEndpoint(
            String url, String endpoint, int retryAmount, int sleepDuration) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");

        for (int i = 0; i < retryAmount; i++) {
            log.info("Trying to fetch callbacks for " + endpoint);
            Optional<List<String>> callbackList = fetchCallbacksForEndpoint(url, endpoint);

            if (callbackList.isPresent()) {
                log.info("Callbacks for " + endpoint + " are fetched");
                return convertToListOfJsonNodes(callbackList.get());
            }
            Uninterruptibles.sleepUninterruptibly(sleepDuration, TimeUnit.SECONDS);
        }
        throw new RuntimeException("Timeout for polling attempt for " + endpoint);
    }

    private static List<JsonNode> convertToListOfJsonNodes(List<String> input) {
        return input.stream()
                .map(
                        data -> {
                            try {
                                return mapper.readValue(data, JsonNode.class);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .collect(Collectors.toList());
    }

    public static String readRequestBodyFromFile(String filePath) {
        try {
            return new String(
                    Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<?> parseAccounts(List<JsonNode> input) {
        return input.stream()
                .map(data -> data.get("account"))
                .map(
                        account -> {
                            try {
                                return mapper.readValue(account.toString(), Map.class);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .collect(Collectors.toList());
    }

    public static List<Map<String, Object>> parseTransactions(List<JsonNode> input) {
        return input.stream()
                .map(data -> data.get("transactions").iterator())
                .map(
                        iterator -> {
                            List<Map<String, Object>> temp = new ArrayList<>();
                            while (iterator.hasNext()) {
                                try {
                                    temp.add(
                                            mapper.readValue(
                                                    iterator.next().toString(), Map.class));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            return temp;
                        })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static Map<String, Object> parseIdentityData(List<JsonNode> input) {
        return input.stream()
                .map(
                        data -> {
                            try {
                                return mapper.readValue(
                                        data.get("identityData").toString(), Map.class);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .findFirst()
                .get();
    }
}
