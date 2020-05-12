package se.tink.backend.aggregation.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.makeGetRequest;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.makePostRequest;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseAccounts;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseIdentityData;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseTransactions;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollForAllCallbacksForAnEndpoint;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollForFinalCredentialsUpdateStatusUntilFlowEnds;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.readRequestBodyFromFile;

import com.fasterxml.jackson.databind.JsonNode;
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

    private static final String APP_UNDER_TEST_HOST = "aggregation_service";
    private static final int APP_UNDER_TEST_PORT = 9095;

    private static final String AGGREGATION_CONTROLLER_HOST = "fake_aggregation_controller";
    private static final int AGGREGATION_CONTROLLER_PORT = 8080;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static AgentContractEntitiesJsonFileParser contractParser =
            new AgentContractEntitiesJsonFileParser();

    private static final String aggregationControllerEndpoint =
            String.format(
                    "http://%s:%d/data", AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT);

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
                        "data/agents/uk/amex/system_test_authenticate_request_body.json");

        // when
        ResponseEntity<String> authenticateEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/authenticate",
                                APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT),
                        requestBodyForAuthenticateEndpoint);

        Optional<String> finalStatusForCredentials =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        String.format(
                                "http://%s:%d/data",
                                AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT),
                        50,
                        1);

        // then
        Assert.assertEquals(204, authenticateEndpointCallResult.getStatusCodeValue());
        Assert.assertTrue(finalStatusForCredentials.isPresent());
        Assert.assertEquals("UPDATED", finalStatusForCredentials.get());
    }

    @Test
    public void getAuthenticateForBarclaysShouldSetCredentialsStatusUpdated() throws Exception {
        // given
        String requestBodyForAuthenticateEndpoint =
                readRequestBodyFromFile(
                        "data/agents/uk/barclays/system_test_authenticate_request_body.json");

        // when
        ResponseEntity<String> authenticateEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/authenticate",
                                APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT),
                        requestBodyForAuthenticateEndpoint);

        Optional<String> finalStatusForCredentials =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        String.format(
                                "http://%s:%d/data",
                                AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT),
                        50,
                        1);

        // then
        Assert.assertEquals(204, authenticateEndpointCallResult.getStatusCodeValue());
        Assert.assertTrue(finalStatusForCredentials.isPresent());
        Assert.assertEquals("UPDATED", finalStatusForCredentials.get());
    }

    @Test
    public void getRefreshShouldUploadEntitiesForAmex() throws Exception {
        // given
        AgentContractEntity expectedBankEntities =
                contractParser.parseContractOnBasisOfFile(
                        "data/agents/uk/amex/system_test_refresh_request_expected_entities.json");

        List<Map<String, Object>> expectedTransactions = expectedBankEntities.getTransactions();
        List<Map<String, Object>> expectedAccounts = expectedBankEntities.getAccounts();
        Map<String, Object> expectedIdentityData =
                expectedBankEntities.getIdentityData().orElseGet(Collections::emptyMap);

        String requestBodyForRefreshEndpoint =
                readRequestBodyFromFile(
                        "data/agents/uk/amex/system_test_refresh_request_body.json");

        // when
        ResponseEntity<String> refreshEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/refresh",
                                APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT),
                        requestBodyForRefreshEndpoint);

        List<?> givenAccounts =
                parseAccounts(
                        pollForAllCallbacksForAnEndpoint(
                                aggregationControllerEndpoint, "updateAccount", 50, 1));

        List<Map<String, Object>> givenTransactions =
                parseTransactions(
                        pollForAllCallbacksForAnEndpoint(
                                aggregationControllerEndpoint,
                                "updateTransactionsAsynchronously",
                                50,
                                1));

        Map<String, Object> givenIdentityData =
                parseIdentityData(
                        pollForAllCallbacksForAnEndpoint(
                                aggregationControllerEndpoint, "updateIdentity", 50, 1));

        // then
        Assert.assertEquals(204, refreshEndpointCallResult.getStatusCodeValue());
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

    @Test
    public void getRefreshShouldUploadEntitiesForBarclays() throws Exception {
        // given
        AgentContractEntity expectedBankEntities =
                contractParser.parseContractOnBasisOfFile(
                        "data/agents/uk/barclays/system_test_refresh_request_expected_entities.json");

        List<Map<String, Object>> expectedTransactions = expectedBankEntities.getTransactions();
        List<Map<String, Object>> expectedAccounts = expectedBankEntities.getAccounts();
        Map<String, Object> expectedIdentityData =
                expectedBankEntities.getIdentityData().orElseGet(Collections::emptyMap);

        String requestBodyForRefreshEndpoint =
                readRequestBodyFromFile(
                        "data/agents/uk/barclays/system_test_refresh_request_body.json");

        // when
        ResponseEntity<String> refreshEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/refresh",
                                APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT),
                        requestBodyForRefreshEndpoint);

        List<?> givenAccounts =
                parseAccounts(
                        pollForAllCallbacksForAnEndpoint(
                                aggregationControllerEndpoint, "updateAccount", 50, 1));

        List<Map<String, Object>> givenTransactions =
                parseTransactions(
                        pollForAllCallbacksForAnEndpoint(
                                aggregationControllerEndpoint,
                                "updateTransactionsAsynchronously",
                                50,
                                1));

        // then
        Assert.assertEquals(204, refreshEndpointCallResult.getStatusCodeValue());
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedTransactions, givenTransactions));
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedAccounts, givenAccounts));
    }

    @Test
    public void getTransferShouldExecuteAPaymentForBarclays() throws Exception {
        // given
        String requestBodyForTransferEndpoint =
                readRequestBodyFromFile(
                        "data/agents/uk/barclays/system_test_transfer_request_body.json");

        // when
        ResponseEntity<String> transferEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/transfer",
                                APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT),
                        requestBodyForTransferEndpoint);

        Optional<String> finalStatusForCredentials =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        String.format(
                                "http://%s:%d/data",
                                AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT),
                        50,
                        1);

        List<JsonNode> credentialsCallbacks =
                pollForAllCallbacksForAnEndpoint(
                        aggregationControllerEndpoint, "updateCredentials", 50, 1);
        JsonNode lastCallbackForCredentials =
                credentialsCallbacks.get(credentialsCallbacks.size() - 1);

        // then
        Assert.assertEquals(204, transferEndpointCallResult.getStatusCodeValue());
        Assert.assertTrue(finalStatusForCredentials.isPresent());
        Assert.assertEquals("UPDATED", finalStatusForCredentials.get());
        Assert.assertEquals("TRANSFER", lastCallbackForCredentials.get("requestType").asText());
    }
}
