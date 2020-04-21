package se.tink.backend.aggregation.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.service.SystemTestUtils.makeGetRequest;
import static se.tink.backend.aggregation.service.SystemTestUtils.makePostRequest;
import static se.tink.backend.aggregation.service.SystemTestUtils.parseAccounts;
import static se.tink.backend.aggregation.service.SystemTestUtils.parseIdentityData;
import static se.tink.backend.aggregation.service.SystemTestUtils.parseTransactions;
import static se.tink.backend.aggregation.service.SystemTestUtils.pollForAllCallbacksForAnEndpoint;
import static se.tink.backend.aggregation.service.SystemTestUtils.pollForFinalCredentialsUpdateStatusUntilFlowEnds;
import static se.tink.backend.aggregation.service.SystemTestUtils.readRequestBodyFromFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;

public class SystemTest {

    private static final String APP_UNDER_TEST_HOST = "appundertest";
    private static final int APP_UNDER_TEST_PORT = 9095;

    private static final String AGGREGATION_CONTROLLER_HOST = "appundertest";
    private static final int AGGREGATION_CONTROLLER_PORT = 8080;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static AgentContractEntitiesJsonFileParser contractParser =
            new AgentContractEntitiesJsonFileParser();

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
    public void getAuthenticateForAmexShouldSetCredentialsStatusUpdated() throws Exception {

        // given
        String requestBodyForAuthenticateEndpoint =
                readRequestBodyFromFile(
                        "src/aggregation/service/src/test/java/se/tink/backend/aggregation/service/resources/authenticate_request_body_for_amex.json");

        // when
        ResponseEntity<String> authenticationCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/authenticate",
                                APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT),
                        requestBodyForAuthenticateEndpoint);

        Optional<String> updateCredentialsCallback =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        String.format(
                                "http://%s:%d/data",
                                AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT));

        // then
        Assert.assertEquals(204, authenticationCallResult.getStatusCodeValue());
        Assert.assertTrue(updateCredentialsCallback.isPresent());
        Assert.assertEquals("UPDATED", updateCredentialsCallback.get());
    }

    @Test
    public void getAuthenticateForBarclaysShouldSetCredentialsStatusUpdated() throws Exception {

        // given
        String requestBodyForAuthenticateEndpoint =
                readRequestBodyFromFile(
                        "src/aggregation/service/src/test/java/se/tink/backend/aggregation/service/resources/authenticate_request_body_for_barclays.json");

        // when
        ResponseEntity<String> authenticationCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/authenticate",
                                APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT),
                        requestBodyForAuthenticateEndpoint);

        Optional<String> updateCredentialsCallback =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        String.format(
                                "http://%s:%d/data",
                                AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT));

        // then
        Assert.assertEquals(204, authenticationCallResult.getStatusCodeValue());
        Assert.assertTrue(updateCredentialsCallback.isPresent());
        Assert.assertEquals("UPDATED", updateCredentialsCallback.get());
    }

    @Test
    public void getRefreshShouldUploadEntities() throws Exception {

        // given
        AgentContractEntity expected =
                contractParser.parseContractOnBasisOfFile(
                        "src/aggregation/service/src/test/java/se/tink/backend/aggregation/service/resources/refresh_request_expected_entities.json");

        List<Map<String, Object>> expectedTransactions = expected.getTransactions();
        List<Map<String, Object>> expectedAccounts = expected.getAccounts();
        Map<String, Object> expectedIdentityData =
                expected.getIdentityData().orElseGet(Collections::emptyMap);

        String requestBodyForRefreshEndpoint =
                readRequestBodyFromFile(
                        "src/aggregation/service/src/test/java/se/tink/backend/aggregation/service/resources/refresh_request_body.json");

        String aggregationControllerEndpoint =
                String.format(
                        "http://%s:%d/data",
                        AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT);

        // when
        ResponseEntity<String> authenticationCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/refresh",
                                APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT),
                        requestBodyForRefreshEndpoint);

        // Why I need "?"
        List<?> givenAccounts =
                parseAccounts(
                        pollForAllCallbacksForAnEndpoint(
                                aggregationControllerEndpoint, "updateAccount", 100, 1));

        List<Map<String, Object>> givenTransactions =
                parseTransactions(
                        pollForAllCallbacksForAnEndpoint(
                                aggregationControllerEndpoint,
                                "updateTransactionsAsynchronously",
                                100,
                                1));

        Map<String, Object> givenIdentityData =
                parseIdentityData(
                        pollForAllCallbacksForAnEndpoint(
                                aggregationControllerEndpoint, "updateIdentity", 100, 1));

        // then
        Assert.assertEquals(204, authenticationCallResult.getStatusCodeValue());

        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedTransactions, givenTransactions));

        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedAccounts, givenAccounts));

        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        Collections.singletonList(expectedIdentityData),
                        Collections.singletonList(givenIdentityData)));
    }
}
