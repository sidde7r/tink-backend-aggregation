package se.tink.backend.aggregation.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class SystemTest {

    private static final String APP_UNDER_TEST_HOST = "appundertest";
    private static final int APP_UNDER_TEST_PORT = 9095;

    private static final String AGGREGATION_CONTROLLER_HOST = "appundertest";
    private static final int AGGREGATION_CONTROLLER_PORT = 8080;

    @Test
    public void getPingShouldReturnPongAnd200MessageInHttpResponse() throws Exception {
        // given
        String url =
                String.format(
                        "http://%s:%d/aggregation/ping", APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT);

        // when
        ResponseEntity<String> response = makeGetRequest(url, new HttpHeaders());

        // then
        assertThat(response.getBody(), equalTo("pong"));
        assertThat(response.getStatusCodeValue(), equalTo(200));
    }

    private ResponseEntity<String> makePostRequest(String url, Object requestBody)
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

    private ResponseEntity<String> makeGetRequest(String url, HttpHeaders headers)
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

    @Test
    public void getAuthenticateShouldSetCredentialsStatusUpdated() throws Exception {
        String requestBody =
                "{\"manual\": true, \"create\": true, \"callbackUri\": \"callbackUri\", \"appUriId\": \"appUriId\", \"userDeviceId\": \"userDeviceId\", \"credentials\": {\"debugUntil\": 695260800, \"providerLatency\": 0, \"id\": \"aaaa\", \"nextUpdate\": null, \"fields\": {\"username\": \"testUser\", \"password\": \"testPassword\"}, \"payload\": \"\", \"providerName\": \"uk-americanexpress-password\", \"sessionExpiryDate\": null, \"status\": \"CREATED\", \"statusPayload\": \"CREATED\", \"statusPrompt\": \"\", \"statusUpdated\": null, \"supplementalInformation\": \"\", \"type\": \"PASSWORD\", \"updated\": null, \"userId\": \"userId\", \"dataVersion\": 1 }, \"provider\": {\"accessType\": \"OTHER\", \"className\": \"nxgen.uk.creditcards.amex.v62.AmericanExpressV62UKAgent\", \"credentialsType\": \"PASSWORD\", \"displayName\": \"American Express\", \"fields\": [{\"description\":\"Username\",\"immutable\":true,\"minLength\":1,\"name\":\"username\"},{\"description\":\"Password\",\"masked\":true,\"minLength\":1,\"name\":\"password\",\"sensitive\":true}], \"supplementalFields\": [], \"financialInstitutionId\": \"dummyId\", \"financialInstitutionName\": \"dummyName\", \"groupDisplayName\": \"American Express\", \"multiFactor\": false, \"name\": \"uk-americanexpress-password\", \"passwordhelptext\": \"Use the same password as you would in your banks mobile app.\", \"popular\": true, \"refreshFrequency\": 1, \"refreshFrequencyFactor\": 1, \"status\": \"ENABLED\", \"transactional\": true, \"type\": \"CREDIT_CARD\", \"currency\": \"GBP\", \"market\": \"UK\", \"payload\": \"\"}, \"user\": {\"flags\": [], \"flagsSerialized\": \"[]\", \"id\": \"userId\", \"profile\": {\"locale\": \"en_US\"}, \"username\": \"username\", \"debugUntil\": 695260800 }, \"accounts\": [] }";

        ResponseEntity<String> authenticationCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/authenticate",
                                APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT),
                        requestBody);

        Assert.assertEquals(204, authenticationCallResult.getStatusCodeValue());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");

        Map<String, List<String>> pushedData = new HashMap<>();
        while (pushedData.keySet().size() == 0) {

            ResponseEntity<String> dataResult =
                    makeGetRequest(
                            String.format(
                                    "http://%s:%d/data",
                                    AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT),
                            headers);

            pushedData = new ObjectMapper().readValue(dataResult.getBody(), Map.class);
            if (pushedData.keySet().size() == 0) {
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                continue;
            }

            List<String> credentialsUpdateCallbacks = pushedData.get("updateCredentials");
            String latestCredentialsUpdate =
                    credentialsUpdateCallbacks.get(credentialsUpdateCallbacks.size() - 1);
            JsonNode node = new ObjectMapper().readTree(latestCredentialsUpdate);
            String credentialsStatus = node.get("credentials").get("status").asText();
            if (!credentialsStatus.equals("UPDATED")) {
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                continue;
            }
            Assert.assertEquals("UPDATED", credentialsStatus);
        }
    }
}
