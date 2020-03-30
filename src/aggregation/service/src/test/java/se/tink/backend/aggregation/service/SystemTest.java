package se.tink.backend.aggregation.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.service.SystemTestUtils.makeGetRequest;
import static se.tink.backend.aggregation.service.SystemTestUtils.makePostRequest;
import static se.tink.backend.aggregation.service.SystemTestUtils.pollAggregationController;
import static se.tink.backend.aggregation.service.SystemTestUtils.readRequestBodyFromFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
import se.tink.backend.aggregation.service.SystemTestUtils.ExpectedCredentialsStatus;

public class SystemTest {

    private static final String APP_UNDER_TEST_HOST = "appundertest";
    private static final int APP_UNDER_TEST_PORT = 9095;

    private static final String AGGREGATION_CONTROLLER_HOST = "appundertest";
    private static final int AGGREGATION_CONTROLLER_PORT = 8080;

    private static final ObjectMapper mapper = new ObjectMapper();

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

        Map<String, List<String>> pushedData =
                pollAggregationController(
                        String.format(
                                "http://%s:%d/data",
                                AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT),
                        Optional.of(ExpectedCredentialsStatus.UPDATED),
                        Collections.emptySet());
    }

    @Test
    public void getRefreshShouldUploadAccountsAndTransactions() throws Exception {

        AgentContractEntitiesJsonFileParser contractParser =
                new AgentContractEntitiesJsonFileParser();
        AgentContractEntity expected =
                contractParser.parseContractOnBasisOfFile(
                        "src/aggregation/service/src/test/java/se/tink/backend/aggregation/service/resources/refresh_request_expected_entities.json");

        List<Map<String, Object>> expectedTransactions = expected.getTransactions();
        List<Map<String, Object>> expectedAccounts = expected.getAccounts();
        Map<String, Object> expectedIdentityData = expected.getIdentityData();

        String requestBody =
                readRequestBodyFromFile(
                        "src/aggregation/service/src/test/java/se/tink/backend/aggregation/service/resources/refresh_request_body.json");

        ResponseEntity<String> authenticationCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/refresh",
                                APP_UNDER_TEST_HOST, APP_UNDER_TEST_PORT),
                        requestBody);

        Assert.assertEquals(204, authenticationCallResult.getStatusCodeValue());

        Map<String, List<String>> pushedData =
                pollAggregationController(
                        String.format(
                                "http://%s:%d/data",
                                AGGREGATION_CONTROLLER_HOST, AGGREGATION_CONTROLLER_PORT),
                        Optional.empty(),
                        ImmutableSet.of(
                                "updateCredentials",
                                "updateTransactionsAsynchronously",
                                "updateAccount",
                                "updateIdentity"));

        // Check transactions
        List<Map<String, Object>> givenTransactions = new ArrayList<>();

        for (int i = 0; i < pushedData.get("updateTransactionsAsynchronously").size(); i++) {
            JsonNode node =
                    mapper.readValue(
                            pushedData.get("updateTransactionsAsynchronously").get(i),
                            JsonNode.class);

            Iterator<JsonNode> transactionsIterator = node.get("transactions").iterator();

            while (transactionsIterator.hasNext()) {
                givenTransactions.add(
                        mapper.readValue(transactionsIterator.next().toString(), Map.class));
            }
        }

        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedTransactions, givenTransactions));

        // Check accounts
        List<Map<String, Object>> givenAccounts = new ArrayList<>();

        for (int i = 0; i < pushedData.get("updateAccount").size(); i++) {
            JsonNode node =
                    mapper.readValue(pushedData.get("updateAccount").get(i), JsonNode.class);
            JsonNode account = node.get("account");
            givenAccounts.add(mapper.readValue(account.toString(), Map.class));
        }

        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedAccounts, givenAccounts));

        // Check identity data
        Map<String, Object> givenIdentityData =
                mapper.readValue(
                        mapper.readValue(
                                        pushedData
                                                .get("updateIdentity")
                                                .get(pushedData.get("updateIdentity").size() - 1),
                                        JsonNode.class)
                                .get("identityData")
                                .toString(),
                        Map.class);

        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        Collections.singletonList(expectedIdentityData),
                        Collections.singletonList(givenIdentityData)));
    }
}
