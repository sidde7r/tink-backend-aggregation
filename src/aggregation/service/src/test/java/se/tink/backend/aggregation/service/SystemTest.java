package se.tink.backend.aggregation.service;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.getFinalFakeBankServerState;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.makeGetRequest;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.makePostRequest;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseAccounts;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseIdentityData;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseTransactions;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseTransferDestinations;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollForAllCallbacksForAnEndpoint;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollUntilCredentialsUpdateStatusIn;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollUntilFinalCredentialsUpdateStatus;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollUntilFinalSignableOperation;
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
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
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
import se.tink.backend.aggregation.workers.operation.RequestStatus;

/** These tests assume that Docker is running. */
@Slf4j
@RunWith(SpringRunner.class)
@SuppressWarnings("rawtypes")
public class SystemTest {

    private static final Set<String> FINAL_REQUEST_STATUSES =
            ImmutableSet.of(
                    RequestStatus.OPERATION_COMPLETED_WITHOUT_ABORT.name(),
                    RequestStatus.ABORTING_OPERATION_SUCCEEDED.name());

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
    public static void tearDown() throws IOException {
        try {
            Optional.ofNullable(aggregationContainer).ifPresent(GenericContainer::stop);
            Optional.ofNullable(fakeAggregationControllerContainer)
                    .ifPresent(GenericContainer::stop);

            // Just if we don't want to leave any traces behind
            if (client != null) {
                client.removeImageCmd(AggregationDecoupled.IMAGE).exec();
                client.removeImageCmd(FakeAggregationController.IMAGE).exec();
            }
        } catch (Exception e) {
            log.warn("Could not tear-down", e);
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
                .withEnv("OPENSSL_CONF", "/etc/ssl/")
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

        List<JsonNode> credentialStatusResponses =
                pollUntilFinalCredentialsUpdateStatus(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        assertEquals(204, authenticateEndpointCallResult.getStatusCodeValue());
        assertEquals(
                Arrays.asList("AUTHENTICATING", "UPDATING", "UPDATED"),
                getCredentialStatuses(credentialStatusResponses));
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

        List<Map<String, Object>> givenAccounts =
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

        // We assume that we have only one account in expected entities
        String accountId = givenAccounts.get(0).get("id").toString();
        List<String> distinctAccountIdsInTransactions =
                givenTransactions.stream()
                        .map(trx -> trx.get("accountId").toString())
                        .distinct()
                        .collect(Collectors.toList());

        // then
        Assert.assertEquals(1, givenAccounts.size());
        Assert.assertEquals(1, distinctAccountIdsInTransactions.size());
        Assert.assertEquals(accountId, distinctAccountIdsInTransactions.get(0));
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

        List<JsonNode> credentialStatusResponses =
                pollUntilFinalCredentialsUpdateStatus(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        assertEquals(204, authenticateEndpointCallResult.getStatusCodeValue());
        assertEquals(
                Arrays.asList(
                        "AUTHENTICATING",
                        "AWAITING_THIRD_PARTY_APP_AUTHENTICATION",
                        "UPDATING",
                        "UPDATED"),
                getCredentialStatuses(credentialStatusResponses));
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
        List<Map<String, Object>> expectedTransferDestinations =
                expectedBankEntities.getTransferDestinationPatterns();

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

        List<Map<String, Object>> givenAccounts =
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

        List<Map<String, Object>> givenTransferDestinations =
                parseTransferDestinations(
                        pollForAllCallbacksForAnEndpoint(
                                fakeAggregationControllerDataEndpoint(),
                                "updateTransferDestinationPatterns",
                                50,
                                1));

        // We assume that we have only one account in expected entities
        String accountId = givenAccounts.get(0).get("id").toString();
        List<String> distinctAccountIdsInTransactions =
                givenTransactions.stream()
                        .map(trx -> trx.get("accountId").toString())
                        .distinct()
                        .collect(Collectors.toList());

        // then
        Assert.assertEquals(1, givenAccounts.size());
        Assert.assertEquals(1, distinctAccountIdsInTransactions.size());
        Assert.assertEquals(accountId, distinctAccountIdsInTransactions.get(0));
        Assert.assertEquals(204, refreshEndpointCallResult.getStatusCodeValue());
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedTransactions, givenTransactions));
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedAccounts, givenAccounts));
        log.info("expectedTransferDestinations: " + expectedTransferDestinations);
        log.info("givenTransferDestinations: " + givenTransferDestinations);

        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedTransferDestinations, givenTransferDestinations));
    }

    /*
       The main purpose of this test is to reproduce the following error that we had for
       DanskeBank agent when we tried to execute it in Java11 container and verify that it
       is fixed.
    */
    @Test
    public void getRefreshShouldUploadEntitiesForDanskeBank() throws Exception {
        // given
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(AggregationDecoupled.HTTP_PORT);

        AgentContractEntitiesJsonFileParser contractParser =
                new AgentContractEntitiesJsonFileParser();
        AgentContractEntity expectedBankEntities =
                contractParser.parseContractOnBasisOfFile(
                        "data/agents/se/danskebank/danskebank-agent-contract-for-system-test.json");

        List<Map<String, Object>> expectedTransactions = expectedBankEntities.getTransactions();
        List<Map<String, Object>> expectedAccounts = expectedBankEntities.getAccounts();

        String requestBodyForRefreshEndpoint =
                readRequestBodyFromFile(
                        "data/agents/se/danskebank/system_test_refresh_request_body.json");

        // when
        ResponseEntity<String> refreshEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/refresh",
                                aggregationHost, aggregationPort),
                        requestBodyForRefreshEndpoint);

        List<Map<String, Object>> givenAccounts =
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

    /*
       This test was failing to execute PhantomJS before but after the changes now
       we are able to execute it. However, since the AAP file used in this test is
       not created properly, the test fails. AAP file should be created and the test
       should be enabled. However, we could verify that PhantomJS works fine with Java11
       container after the changes. The following files needs to be completed to make this
       test work:

       - data/agents/dk/nordea/nordeabank-agent-contract-for-system-test.json
       - data/agents/dk/nordea/dk-nordea-nemid-mock.aap
    */
    @Test
    @Ignore
    public void getRefreshShouldUploadEntitiesForNordeaBank() throws Exception {
        // given
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(AggregationDecoupled.HTTP_PORT);

        AgentContractEntitiesJsonFileParser contractParser =
                new AgentContractEntitiesJsonFileParser();
        AgentContractEntity expectedBankEntities =
                contractParser.parseContractOnBasisOfFile(
                        "data/agents/dk/nordea/nordeabank-agent-contract-for-system-test.json");

        List<Map<String, Object>> expectedTransactions = expectedBankEntities.getTransactions();
        List<Map<String, Object>> expectedAccounts = expectedBankEntities.getAccounts();

        String requestBodyForRefreshEndpoint =
                readRequestBodyFromFile(
                        "data/agents/dk/nordea/system_test_refresh_request_body.json");

        // when
        ResponseEntity<String> refreshEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/refresh",
                                aggregationHost, aggregationPort),
                        requestBodyForRefreshEndpoint);

        List<Map<String, Object>> givenAccounts =
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

        List<JsonNode> credentialStatusResponses =
                pollUntilFinalCredentialsUpdateStatus(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        List<JsonNode> signableOperationResponses =
                pollUntilFinalSignableOperation(fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        assertEquals(204, transferEndpointCallResult.getStatusCodeValue());
        assertEquals(
                Arrays.asList("AUTHENTICATING", "UPDATING", "UPDATED"),
                getCredentialStatuses(credentialStatusResponses));
        assertTrue(
                credentialStatusResponses.stream()
                        .map(response -> response.get("requestType").asText())
                        .allMatch("TRANSFER"::equals));
        assertEquals(
                Arrays.asList("EXECUTING", "AWAITING_CREDENTIALS", "EXECUTED"),
                getStatuses(signableOperationResponses));
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

        List<JsonNode> credentialStatusResponses =
                pollUntilFinalCredentialsUpdateStatus(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        List<JsonNode> signableOperationResponses =
                pollUntilFinalSignableOperation(fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        assertEquals(204, transferEndpointCallResult.getStatusCodeValue());
        assertEquals(
                Arrays.asList(
                        "AUTHENTICATING",
                        "AWAITING_THIRD_PARTY_APP_AUTHENTICATION",
                        "UPDATING",
                        "UPDATED"),
                getCredentialStatuses(credentialStatusResponses));
        assertTrue(
                credentialStatusResponses.stream()
                        .map(response -> response.get("requestType").asText())
                        .allMatch("TRANSFER"::equals));
        assertEquals(
                Arrays.asList(
                        "AWAITING_CREDENTIALS",
                        "AWAITING_CREDENTIALS",
                        "EXECUTING",
                        "AWAITING_CREDENTIALS",
                        "EXECUTED"),
                getStatuses(signableOperationResponses));
        String credentialsId = "3891be5ee8e24529ae20d647c035bb0a";
        assertEquals(
                "TRANSFER_STATUS_FETCHED",
                getFinalFakeBankServerState(
                        fakeAggregationControllerFakeBankStateEndpoint(), credentialsId));
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
        String credentialsId = "f35c077b3fd744819bd2ac4ad3b6001e";
        ResponseEntity<String> transferEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/payment",
                                aggregationHost, aggregationPort),
                        requestBodyForTransferEndpoint,
                        credentialsId);

        List<String> operationStatuses =
                pollAbortEndpointUntilReceivingFinalStatus(
                        aggregationHost,
                        aggregationPort,
                        credentialsId,
                        Duration.ofSeconds(5),
                        Duration.ofMillis(100));

        List<JsonNode> credentialStatusResponses =
                pollUntilFinalCredentialsUpdateStatus(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        List<JsonNode> signableOperationResponses =
                pollUntilFinalSignableOperation(fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        assertEquals(
                RequestStatus.ABORTING_OPERATION_SUCCEEDED.name(),
                operationStatuses.get(operationStatuses.size() - 1));
        assertEquals(204, transferEndpointCallResult.getStatusCodeValue());
        assertEquals(
                Arrays.asList(
                        "AUTHENTICATING", "AWAITING_THIRD_PARTY_APP_AUTHENTICATION", "UNCHANGED"),
                getCredentialStatuses(credentialStatusResponses));
        assertTrue(
                credentialStatusResponses.stream()
                        .map(response -> response.get("requestType").asText())
                        .allMatch("TRANSFER"::equals));
        assertEquals(
                Arrays.asList("AWAITING_CREDENTIALS", "AWAITING_CREDENTIALS", "CANCELLED"),
                getStatuses(signableOperationResponses));
        assertEquals(
                "CONSENT_CREATED",
                getFinalFakeBankServerState(
                        fakeAggregationControllerFakeBankStateEndpoint(), credentialsId));
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
        String credentialsId = "af1a7e5cce1341a78aac993539f71922";
        ResponseEntity<String> transferEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/payment",
                                aggregationHost, aggregationPort),
                        requestBodyForTransferEndpoint,
                        credentialsId);

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

        List<String> operationStatuses =
                pollAbortEndpointUntilReceivingFinalStatus(
                        aggregationHost,
                        aggregationPort,
                        credentialsId,
                        Duration.ofSeconds(10),
                        Duration.ofMillis(100));
        assertFalse(operationStatuses.isEmpty());

        List<JsonNode> credentialStatusResponses =
                pollUntilFinalCredentialsUpdateStatus(
                        fakeAggregationControllerDataEndpoint(), 50, 1);

        List<JsonNode> signableOperationResponses =
                pollUntilFinalSignableOperation(fakeAggregationControllerDataEndpoint(), 50, 1);

        // then
        assertEquals(
                RequestStatus.OPERATION_COMPLETED_WITHOUT_ABORT.name(),
                operationStatuses.get(operationStatuses.size() - 1));
        assertEquals(204, transferEndpointCallResult.getStatusCodeValue());
        assertEquals(
                Arrays.asList(
                        "AUTHENTICATING",
                        "AWAITING_THIRD_PARTY_APP_AUTHENTICATION",
                        "UPDATING",
                        "UPDATED"),
                getCredentialStatuses(credentialStatusResponses));
        assertTrue(
                credentialStatusResponses.stream()
                        .map(response -> response.get("requestType").asText())
                        .allMatch("TRANSFER"::equals));
        assertEquals(
                Arrays.asList(
                        "AWAITING_CREDENTIALS",
                        "AWAITING_CREDENTIALS",
                        "EXECUTING",
                        "AWAITING_CREDENTIALS",
                        "EXECUTED"),
                getStatuses(signableOperationResponses));
        assertEquals(
                "TRANSFER_STATUS_FETCHED",
                getFinalFakeBankServerState(
                        fakeAggregationControllerFakeBankStateEndpoint(), credentialsId));
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

    private JsonNode abortOperation(
            String aggregationHost, int aggregationPort, String credentialsId) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(
                makeGetRequest(
                                String.format(
                                        "http://%s:%d/aggregation/operations/abort?credentials_id=%s",
                                        aggregationHost, aggregationPort, credentialsId),
                                new HttpHeaders())
                        .getBody());
    }

    private List<String> pollAbortEndpointUntilReceivingFinalStatus(
            String aggregationHost,
            int aggregationPort,
            String credentialsId,
            Duration timeout,
            Duration poolInterval)
            throws Exception {
        String requestStatus;
        Instant start = Instant.now();
        LinkedList<String> requestStatuses = new LinkedList<>();

        while (true) {
            if (Duration.between(start, Instant.now()).compareTo(timeout) > 0) {
                throw new TimeoutException(
                        "Timeout while polling final request status, received statues "
                                + requestStatuses);
            }

            JsonNode responseBody;
            try {
                responseBody = abortOperation(aggregationHost, aggregationPort, credentialsId);
            } catch (Exception e) {
                log.warn("Received exception while trying to abort an operation", e);
                waitFor(poolInterval);
                continue;
            }
            requestStatus = responseBody.get("requestStatus").asText();

            if (requestStatuses.isEmpty() || !requestStatuses.getLast().equals(requestStatus)) {
                requestStatuses.add(requestStatus);
            }

            if (FINAL_REQUEST_STATUSES.contains(requestStatus)) {
                break;
            }
            waitFor(poolInterval);
        }
        return requestStatuses;
    }

    private static List<String> getCredentialStatuses(List<JsonNode> credentialStatusResponses) {
        return credentialStatusResponses.stream()
                .map(response -> response.get("credentials").get("status").asText())
                .collect(Collectors.toList());
    }

    private static List<String> getStatuses(List<JsonNode> operations) {
        return operations.stream()
                .map(response -> response.get("status").asText())
                .collect(Collectors.toList());
    }

    private static void waitFor(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }
}
