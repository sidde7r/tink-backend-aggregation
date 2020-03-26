package se.tink.backend.aggregation.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.service.SystemTestUtils.makeGetRequest;
import static se.tink.backend.aggregation.service.SystemTestUtils.makePostRequest;
import static se.tink.backend.aggregation.service.SystemTestUtils.pollAggregationController;
import static se.tink.backend.aggregation.service.SystemTestUtils.readRequestBodyFromFile;

import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class SystemTest {

    private static final String APP_UNDER_TEST_HOST = "appundertest";
    private static final int APP_UNDER_TEST_PORT = 9095;

    private static final String AGGREGATION_CONTROLLER_HOST = "appundertest";
    private static final int AGGREGATION_CONTROLLER_PORT = 8080;

    @Before
    public void resetFakeAggregationControllerCache() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        ResponseEntity<String> dataResult =
                makeGetRequest(
                        String.format(
                                "http://%s:%d/reset",
                                AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT),
                        headers);
    }

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

    @Test
    public void getAuthenticateShouldSetCredentialsStatusUpdated() throws Exception {
        String requestBody =
                readRequestBodyFromFile(
                        "src/aggregation/service/src/test/java/se/tink/backend/aggregation/service/resources/authenticate_request_body.json");

        ResponseEntity<String> authenticationCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/authenticate",
                                APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT),
                        requestBody);

        Assert.assertEquals(204, authenticationCallResult.getStatusCodeValue());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");

        Map<String, List<String>> pushedData =
                pollAggregationController(
                        String.format(
                                "http://%s:%d/data",
                                AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT),
                        headers,
                        "UPDATED",
                        null);
    }
}
