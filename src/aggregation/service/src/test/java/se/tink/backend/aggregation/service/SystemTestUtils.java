package se.tink.backend.aggregation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

    public static enum ExpectedCredentialsStatus {
        TEMPORARY_ERROR,
        UPDATED
    };

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

    public static Map<String, List<String>> pollAggregationController(
            String url,
            Optional<ExpectedCredentialsStatus> expectedCredentialsStatus,
            Set<String> expectedCallbacks)
            throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");

        Map<String, List<String>> pushedData = new HashMap<>();
        while (true) {

            ResponseEntity<String> dataResult = makeGetRequest(url, headers);

            pushedData = new ObjectMapper().readValue(dataResult.getBody(), Map.class);
            if (pushedData.keySet().size() == 0) {
                log.info("Waiting for callback");
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                continue;
            }

            if (expectedCredentialsStatus.isPresent()) {
                if (!pushedData.containsKey("updateCredentials")) {
                    log.info("Waiting for updateCredentials");
                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                    continue;
                }
                List<String> credentialsUpdateCallbacks = pushedData.get("updateCredentials");
                JsonNode latestCredentialsUpdateCallback =
                        mapper.readTree(
                                credentialsUpdateCallbacks.get(
                                        credentialsUpdateCallbacks.size() - 1));
                String credentialsStatus =
                        latestCredentialsUpdateCallback.get("credentials").get("status").asText();
                if (!credentialsStatus.equalsIgnoreCase(
                        expectedCredentialsStatus.get().toString())) {
                    log.info("Waiting for credentials to get status " + expectedCredentialsStatus);
                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                    continue;
                }
            }

            boolean isThereMissingCallback = false;
            for (String callbackKey : expectedCallbacks) {
                if (!pushedData.containsKey(callbackKey)) {
                    log.info("Waiting for " + callbackKey);
                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                    isThereMissingCallback = true;
                }
            }
            if (isThereMissingCallback) {
                continue;
            }

            break;
        }
        return pushedData;
    }

    public static String readRequestBodyFromFile(String filePath) {
        try {
            return new String(
                    Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
