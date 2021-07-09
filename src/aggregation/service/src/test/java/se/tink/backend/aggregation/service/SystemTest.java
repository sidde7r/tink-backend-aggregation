package se.tink.backend.aggregation.service;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.getFinalFakeBankServerState;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.makePostRequest;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseAccounts;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseIdentityData;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseTransactions;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollForAllCallbacksForAnEndpoint;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollForFinalCredentialsUpdateStatusUntilFlowEnds;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollForFinalSignableOperation;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollUntilCredentialsUpdateStatusIn;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.postSupplementalInformation;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.readRequestBodyFromFile;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.resetFakeAggregationController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.service.utils.SystemTestUtils;

/** These tests assume that Docker is running. */
@Slf4j
@RunWith(SpringRunner.class)
@SuppressWarnings("rawtypes")
public class SystemTest {

    private static final Set<String> FINAL_OPERATION_STATUSES =
            ImmutableSet.of("ABORTED", "IMPOSSIBLE_TO_ABORT");

    private static class AggregationDecoupled {
        private static final String BASE = "src/aggregation/service";
        private static final String REPOSITORY = "bazel/" + BASE;
        private static final String TAG = "aggregation_system_test_image";
        private static final String TAR = BASE + "/" + TAG + ".tar";
        private static final String IMAGE = REPOSITORY + ":" + TAG;
        private static final int HTTP_PORT = 9095;
    }

    private static class FakeAggregationController {
        private static final String BASE =
                "src/aggregation/fake_aggregation_controller/src/main/java/se/tink/backend/fake_aggregation_controller";
        private static final String TAG = "image";
        private static final String TAR = BASE + "/" + TAG + ".tar";
        private static final String REPOSITORY = "bazel/" + BASE;
        private static final String IMAGE = REPOSITORY + ":" + TAG;
        private static final int HTTP_PORT = 8080;
        private static final String NETWORK_ALIAS = "fakeaggregationcontroller";
    }

    private static GenericContainer aggregationContainer;
    private static GenericContainer fakeAggregationControllerContainer;
    private static DockerClient client;

    @BeforeClass
    public static void setUp() throws FileNotFoundException {
        client = DockerClientFactory.instance().client();

        Network network = Network.newNetwork();

        fakeAggregationControllerContainer = setupAggregationControllerContainer(client, network);
        fakeAggregationControllerContainer.start();

        aggregationContainer = setupAggregationContainer(client, network);
        aggregationContainer.start();
    }

    @Before
    public void reset() throws Exception {
        resetFakeAggregationController(fakeAggregationControllerResetEndpoint());
    }

    @AfterClass
    public static void tearDown() {
        Optional.ofNullable(aggregationContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(fakeAggregationControllerContainer).ifPresent(GenericContainer::stop);

        // Just if we don't want to leave any traces behind
        if (client != null) {
            client.removeImageCmd(AggregationDecoupled.IMAGE).exec();
            client.removeImageCmd(FakeAggregationController.IMAGE).exec();
        }
    }

    private static GenericContainer setupAggregationContainer(DockerClient client, Network network)
            throws FileNotFoundException {
        InputStream aggregationTarStream =
                new BufferedInputStream(new FileInputStream(AggregationDecoupled.TAR));
        client.loadImageCmd(aggregationTarStream).exec();

        ImageFromDockerfile aggregationImage =
                new ImageFromDockerfile()
                        .withDockerfileFromBuilder(
                                builder -> builder.from(AggregationDecoupled.IMAGE));

        final String acSocket = FakeAggregationController.NETWORK_ALIAS + ":8080";

        return new GenericContainer(aggregationImage)
                .waitingFor(
                        Wait.forHttp("/aggregation/ping").forPort(AggregationDecoupled.HTTP_PORT))
                .withEnv("AGGREGATION_CONTROLLER_SOCKET", acSocket)
                .withExposedPorts(AggregationDecoupled.HTTP_PORT)
                .withNetwork(network)
                .withLogConsumer(new Slf4jLogConsumer(log));
    }

    private static GenericContainer setupAggregationControllerContainer(
            DockerClient client, Network network) throws FileNotFoundException {
        InputStream aggregationControllerTarStream =
                new BufferedInputStream(new FileInputStream(FakeAggregationController.TAR));
        client.loadImageCmd(aggregationControllerTarStream).exec();

        ImageFromDockerfile aggregationControllerImage =
                new ImageFromDockerfile()
                        .withDockerfileFromBuilder(
                                builder -> builder.from(FakeAggregationController.IMAGE));
        return new GenericContainer(aggregationControllerImage)
                .waitingFor(Wait.forHttp("/ping").forPort(FakeAggregationController.HTTP_PORT))
                .withExposedPorts(FakeAggregationController.HTTP_PORT)
                .withNetworkAliases(FakeAggregationController.NETWORK_ALIAS)
                .withNetwork(network)
                .withLogConsumer(new Slf4jLogConsumer(log));
    }

    @Test
    public void whenPingedRespondWithPong() {
        // given
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(AggregationDecoupled.HTTP_PORT);
        String url =
                String.format("http://%s:%d/aggregation/ping", aggregationHost, aggregationPort);
        TestRestTemplate testRestTemplate = new TestRestTemplate();

        // when
        String response = testRestTemplate.getForObject(url, String.class);

        // then
        assertThat(response, equalTo("pong"));
    }

    @Test
    public void getAuthenticateForAmexShouldSetCredentialsStatusUpdated() throws Exception {

        // given
        String requestBodyForAuthenticateEndpoint =
                SystemTestUtils.readRequestBodyFromFile(
                        "data/agents/uk/amex/system_test_authenticate_request_body.json");
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(AggregationDecoupled.HTTP_PORT);

        // when
        ResponseEntity<String> authenticateEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/authenticate",
                                aggregationHost, aggregationPort),
                        requestBodyForAuthenticateEndpoint);

        String finalStatusForCredentials =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        Assertions.assertThat(authenticateEndpointCallResult.getStatusCodeValue()).isEqualTo(204);
        Assertions.assertThat(finalStatusForCredentials).isEqualTo("UPDATED");
    }

    @Test
    public void getRefreshShouldUploadEntitiesForAmex() throws Exception {
        // given
        /*
           TODO (AAP-1301): This credentialsId is taken from the JSON file mentioned one line below
           This causes a coupling which should be avoided.
        */
        String givenCredentialsId = "refresh-test";
        AgentContractEntitiesJsonFileParser contractParser =
                new AgentContractEntitiesJsonFileParser();
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

        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(AggregationDecoupled.HTTP_PORT);

        // when
        ResponseEntity<String> refreshEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/refresh",
                                aggregationHost, aggregationPort),
                        requestBodyForRefreshEndpoint);

        List<?> givenAccounts =
                parseAccounts(
                        pollForAllCallbacksForAnEndpoint(
                                fakeAggregationControllerDataEndpoint(), "updateAccount", 50, 1));

        List<Map<String, Object>> givenTransactions =
                parseTransactions(
                        pollForAllCallbacksForAnEndpoint(
                                fakeAggregationControllerDataEndpoint(),
                                "updateTransactionsAsynchronously",
                                50,
                                1));

        Map<String, Object> givenIdentityData =
                parseIdentityData(
                        pollForAllCallbacksForAnEndpoint(
                                fakeAggregationControllerDataEndpoint(), "updateIdentity", 50, 1));

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
        Assertions.assertThat(
                        getFinalFakeBankServerState(
                                fakeAggregationControllerFakeBankStateEndpoint(),
                                givenCredentialsId))
                .isEqualTo("FINAL_STATE");
    }

    @Test
    public void getAuthenticateForBarclaysShouldSetCredentialsStatusUpdated() throws Exception {
        // given
        String requestBodyForAuthenticateEndpoint =
                readRequestBodyFromFile(
                        "data/agents/uk/barclays/system_test_authenticate_request_body.json");
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(AggregationDecoupled.HTTP_PORT);

        // when
        ResponseEntity<String> authenticateEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/authenticate",
                                aggregationHost, aggregationPort),
                        requestBodyForAuthenticateEndpoint);

        String supplementalInformation = "{\"code\":\"DUMMY_AUTH_CODE\"}";
        postSupplementalInformation(
                aggregationHost, aggregationPort, "tpcb_appUriId", supplementalInformation);

        String finalStatusForCredentials =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        Assertions.assertThat(authenticateEndpointCallResult.getStatusCodeValue()).isEqualTo(204);
        Assertions.assertThat(finalStatusForCredentials).isEqualTo("UPDATED");
    }

    @Test
    public void getRefreshShouldUploadEntitiesForBarclays() throws Exception {
        // given
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(AggregationDecoupled.HTTP_PORT);

        AgentContractEntitiesJsonFileParser contractParser =
                new AgentContractEntitiesJsonFileParser();
        AgentContractEntity expectedBankEntities =
                contractParser.parseContractOnBasisOfFile(
                        "data/agents/uk/barclays/system_test_refresh_request_expected_entities.json");

        List<Map<String, Object>> expectedTransactions = expectedBankEntities.getTransactions();
        List<Map<String, Object>> expectedAccounts = expectedBankEntities.getAccounts();

        String requestBodyForRefreshEndpoint =
                readRequestBodyFromFile(
                        "data/agents/uk/barclays/system_test_refresh_request_body.json");

        // when
        ResponseEntity<String> refreshEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/refresh",
                                aggregationHost, aggregationPort),
                        requestBodyForRefreshEndpoint);

        String supplementalInformation = "{\"code\":\"DUMMY_AUTH_CODE\"}";
        postSupplementalInformation(
                aggregationHost, aggregationPort, "tpcb_appUriId", supplementalInformation);

        List<?> givenAccounts =
                parseAccounts(
                        pollForAllCallbacksForAnEndpoint(
                                fakeAggregationControllerDataEndpoint(), "updateAccount", 50, 1));

        List<Map<String, Object>> givenTransactions =
                parseTransactions(
                        pollForAllCallbacksForAnEndpoint(
                                fakeAggregationControllerDataEndpoint(),
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
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(AggregationDecoupled.HTTP_PORT);

        String requestBodyForTransferEndpoint =
                readRequestBodyFromFile(
                        "data/agents/uk/barclays/system_test_transfer_request_body.json");

        // when
        ResponseEntity<String> transferEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/transfer",
                                aggregationHost, aggregationPort),
                        requestBodyForTransferEndpoint);

        String supplementalInformation = "{\"code\":\"DUMMY_AUTH_CODE\"}";
        postSupplementalInformation(
                aggregationHost, aggregationPort, "tpcb_appUriId", supplementalInformation);

        String finalStatusForCredentials =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        List<JsonNode> credentialsCallbacks =
                pollForAllCallbacksForAnEndpoint(
                        fakeAggregationControllerDataEndpoint(), "updateCredentials", 50, 1);
        JsonNode lastCallbackForCredentials =
                credentialsCallbacks.get(credentialsCallbacks.size() - 1);

        JsonNode lastCallbackForSignableOperation =
                pollForFinalSignableOperation(fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        Assertions.assertThat(transferEndpointCallResult.getStatusCodeValue()).isEqualTo(204);
        Assertions.assertThat(finalStatusForCredentials).isEqualTo("UPDATED");
        Assertions.assertThat(lastCallbackForCredentials.get("requestType").asText())
                .isEqualTo("TRANSFER");
        Assertions.assertThat(lastCallbackForSignableOperation.get("status").asText())
                .isEqualTo("EXECUTED");
    }

    @Test
    public void paymentShouldExecuteAPaymentForUnicredit() throws Exception {
        // given
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(AggregationDecoupled.HTTP_PORT);

        String requestBodyForTransferEndpoint =
                readRequestBodyFromFile(
                        "data/agents/it/unicredit/system_test_payment_request_body_1.json");

        // when
        ResponseEntity<String> transferEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/payment",
                                aggregationHost, aggregationPort),
                        requestBodyForTransferEndpoint);

        postSupplementalInformation(
                aggregationHost,
                aggregationPort,
                "tpcb_e79523ae-2eab-4594-9d76-a2f98d38feed",
                "{}");

        String finalStatusForCredentials =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        List<JsonNode> credentialsCallbacks =
                pollForAllCallbacksForAnEndpoint(
                        fakeAggregationControllerDataEndpoint(), "updateCredentials", 50, 1);
        JsonNode lastCallbackForCredentials =
                credentialsCallbacks.get(credentialsCallbacks.size() - 1);

        JsonNode lastCallbackForSignableOperation =
                pollForFinalSignableOperation(fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        Assertions.assertThat(transferEndpointCallResult.getStatusCodeValue()).isEqualTo(204);
        Assertions.assertThat(finalStatusForCredentials).isEqualTo("UPDATED");
        Assertions.assertThat(lastCallbackForCredentials.get("requestType").asText())
                .isEqualTo("TRANSFER");
        Assertions.assertThat(lastCallbackForSignableOperation.get("status").asText())
                .isEqualTo("EXECUTED");
    }

    @Test
    public void abortShouldAbortAPaymentExecutionForUnicredit() throws Exception {
        // given
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(AggregationDecoupled.HTTP_PORT);

        String requestBodyForTransferEndpoint =
                readRequestBodyFromFile(
                        "data/agents/it/unicredit/system_test_payment_request_body_2.json");

        // when
        ResponseEntity<String> transferEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/payment",
                                aggregationHost, aggregationPort),
                        requestBodyForTransferEndpoint);

        String operationId = "795d5477-681c-4c44-a593-7698a9cc646f";
        List<String> operationStatuses =
                abortOperationUntilFinalResult(
                        aggregationHost, aggregationPort, operationId, Duration.ofSeconds(5));
        assertFalse(operationStatuses.isEmpty());

        String finalStatusForCredentials =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        List<JsonNode> credentialsCallbacks =
                pollForAllCallbacksForAnEndpoint(
                        fakeAggregationControllerDataEndpoint(), "updateCredentials", 50, 1);
        JsonNode lastCallbackForCredentials =
                credentialsCallbacks.get(credentialsCallbacks.size() - 1);

        JsonNode lastCallbackForSignableOperation =
                pollForFinalSignableOperation(fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        assertEquals("ABORTED", operationStatuses.get(operationStatuses.size() - 1));
        Assertions.assertThat(transferEndpointCallResult.getStatusCodeValue()).isEqualTo(204);
        Assertions.assertThat(finalStatusForCredentials).isEqualTo("UPDATED");
        Assertions.assertThat(lastCallbackForCredentials.get("requestType").asText())
                .isEqualTo("TRANSFER");
        Assertions.assertThat(lastCallbackForSignableOperation.get("status").asText())
                .isEqualTo("FAILED");
    }

    @Test
    public void abortShouldNotAbortAPaymentExecutionForUnicreditWhenItIsTooLateToAbort()
            throws Exception {
        // given
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(AggregationDecoupled.HTTP_PORT);

        String requestBodyForTransferEndpoint =
                readRequestBodyFromFile(
                        "data/agents/it/unicredit/system_test_payment_request_body_3.json");

        // when
        ResponseEntity<String> transferEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/payment",
                                aggregationHost, aggregationPort),
                        requestBodyForTransferEndpoint);

        postSupplementalInformation(
                aggregationHost,
                aggregationPort,
                "tpcb_e79523ae-2eab-4594-9d76-a2f98d38feed",
                "{}");

        pollUntilCredentialsUpdateStatusIn(
                fakeAggregationControllerDataEndpoint(),
                ImmutableSet.of("UPDATING", "UPDATED"),
                50,
                1);

        String operationId = "5091db36-b11d-4e68-990d-017e8ea935ec";
        List<String> operationStatuses =
                abortOperationUntilFinalResult(
                        aggregationHost, aggregationPort, operationId, Duration.ofSeconds(5));
        assertFalse(operationStatuses.isEmpty());

        String finalStatusForCredentials =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        List<JsonNode> credentialsCallbacks =
                pollForAllCallbacksForAnEndpoint(
                        fakeAggregationControllerDataEndpoint(), "updateCredentials", 50, 1);
        JsonNode lastCallbackForCredentials =
                credentialsCallbacks.get(credentialsCallbacks.size() - 1);

        JsonNode lastCallbackForSignableOperation =
                pollForFinalSignableOperation(fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        assertEquals("IMPOSSIBLE_TO_ABORT", operationStatuses.get(operationStatuses.size() - 1));

        Assertions.assertThat(transferEndpointCallResult.getStatusCodeValue()).isEqualTo(204);
        Assertions.assertThat(finalStatusForCredentials).isEqualTo("UPDATED");
        Assertions.assertThat(lastCallbackForCredentials.get("requestType").asText())
                .isEqualTo("TRANSFER");
        Assertions.assertThat(lastCallbackForSignableOperation.get("status").asText())
                .isEqualTo("EXECUTED");
    }

    private String fakeAggregationControllerResetEndpoint() {
        final String host = fakeAggregationControllerContainer.getContainerIpAddress();
        final int port =
                fakeAggregationControllerContainer.getMappedPort(
                        FakeAggregationController.HTTP_PORT);
        return String.format("http://%s:%d/reset", host, port);
    }

    private String fakeAggregationControllerDataEndpoint() {
        final String host = fakeAggregationControllerContainer.getContainerIpAddress();
        final int port =
                fakeAggregationControllerContainer.getMappedPort(
                        FakeAggregationController.HTTP_PORT);
        return String.format("http://%s:%d/data", host, port);
    }

    private String fakeAggregationControllerFakeBankStateEndpoint() {
        final String host = fakeAggregationControllerContainer.getContainerIpAddress();
        final int port =
                fakeAggregationControllerContainer.getMappedPort(
                        FakeAggregationController.HTTP_PORT);
        return String.format("http://%s:%d/bank_state", host, port);
    }

    private JsonNode abortPayment(String aggregationHost, int aggregationPort, String operationId)
            throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(
                makePostRequest(
                                String.format(
                                        "http://%s:%d/aggregation/payment/%s/aborts",
                                        aggregationHost, aggregationPort, operationId),
                                null)
                        .getBody());
    }

    private List<String> abortOperationUntilFinalResult(
            String aggregationHost, int aggregationPort, String operationId, Duration timeout)
            throws Exception {
        String operationStatus;
        Instant start = Instant.now();
        LinkedList<String> operationStatuses = new LinkedList<>();
        do {
            if (Duration.between(start, Instant.now()).compareTo(timeout) > 0) {
                throw new TimeoutException(
                        "Timeout while polling final operation status, received statues "
                                + operationStatuses);
            }
            JsonNode responseBody = abortPayment(aggregationHost, aggregationPort, operationId);
            operationStatus = responseBody.get("operationStatus").asText();
            if (operationStatuses.isEmpty()
                    || !operationStatuses.getLast().equals(operationStatus)) {
                operationStatuses.add(operationStatus);
            }
        } while (!FINAL_OPERATION_STATUSES.contains(operationStatus));
        return operationStatuses;
    }
}
