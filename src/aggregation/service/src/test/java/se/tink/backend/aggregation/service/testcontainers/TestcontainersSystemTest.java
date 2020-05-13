package se.tink.backend.aggregation.service.testcontainers;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.makePostRequest;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseAccounts;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseIdentityData;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.parseTransactions;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollForAllCallbacksForAnEndpoint;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.pollForFinalCredentialsUpdateStatusUntilFlowEnds;
import static se.tink.backend.aggregation.service.utils.SystemTestUtils.readRequestBodyFromFile;

import com.github.dockerjava.api.DockerClient;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
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

/** These tests assume that the Docker daemon is running. */
@Slf4j
@RunWith(SpringRunner.class)
public class TestcontainersSystemTest {

    private static class Aggregation {
        private static final String BASE = "src/aggregation/service";
        private static final String REPOSITORY = "bazel/" + BASE;
        private static final String TAG = "aggregation_decoupled_image";
        private static final String TAR = BASE + "/" + TAG + ".tar";
        private static final String IMAGE = REPOSITORY + ":" + TAG;
        private static final int HTTP_PORT = 9095;
    }

    private static class AggregationController {
        private static final String BASE =
                "src/aggregation/fake_aggregation_controller/src/main/java/se/tink/backend/fake_aggregation_controller";
        private static final String TAG = "image";
        private static final String TAR = BASE + "/" + TAG + ".tar";
        private static final String REPOSITORY = "bazel/" + BASE;
        private static final String IMAGE = REPOSITORY + ":" + TAG;
        private static final int HTTP_PORT = 8080;
        private static final String NETWORK_ALIAS = "fakeaggregationcontroller";
    }

    private DockerClient client;
    @Rule public GenericContainer aggregationContainer;
    @Rule public GenericContainer aggregationControllerContainer;

    @Before
    public void setup() throws FileNotFoundException {
        client = DockerClientFactory.instance().client();

        Network network = Network.newNetwork();

        aggregationControllerContainer = setupAggregationControllerContainer(client, network);
        aggregationControllerContainer.start();

        aggregationContainer = setupAggregationContainer(client, network);
        aggregationContainer.start();
    }

    private static GenericContainer setupAggregationContainer(DockerClient client, Network network)
            throws FileNotFoundException {
        InputStream aggregationTarStream =
                new BufferedInputStream(new FileInputStream(Aggregation.TAR));
        client.loadImageCmd(aggregationTarStream).exec();

        ImageFromDockerfile aggregationImage =
                new ImageFromDockerfile()
                        .withDockerfileFromBuilder(builder -> builder.from(Aggregation.IMAGE));

        final String acSocket = AggregationController.NETWORK_ALIAS + ":8080";

        return new GenericContainer(aggregationImage)
                .waitingFor(Wait.forHttp("/aggregation/ping").forPort(Aggregation.HTTP_PORT))
                .withEnv("AGGREGATION_CONTROLLER_SOCKET", acSocket)
                .withExposedPorts(Aggregation.HTTP_PORT)
                .withNetwork(network)
                .withLogConsumer(new Slf4jLogConsumer(log));
    }

    private static GenericContainer setupAggregationControllerContainer(
            DockerClient client, Network network) throws FileNotFoundException {
        InputStream aggregationControllerTarStream =
                new BufferedInputStream(new FileInputStream(AggregationController.TAR));
        client.loadImageCmd(aggregationControllerTarStream).exec();

        ImageFromDockerfile aggregationControllerImage =
                new ImageFromDockerfile()
                        .withDockerfileFromBuilder(
                                builder -> builder.from(AggregationController.IMAGE));
        return new GenericContainer(aggregationControllerImage)
                .waitingFor(Wait.forHttp("/ping").forPort(AggregationController.HTTP_PORT))
                .withExposedPorts(AggregationController.HTTP_PORT)
                .withNetworkAliases(AggregationController.NETWORK_ALIAS)
                .withNetwork(network)
                .withLogConsumer(new Slf4jLogConsumer(log));
    }

    @Test
    public void whenPingedRespondWithPong() {
        // given
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(Aggregation.HTTP_PORT);
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
        int aggregationPort = aggregationContainer.getMappedPort(Aggregation.HTTP_PORT);

        String aggregationControllerHost = aggregationControllerContainer.getContainerIpAddress();
        int aggregationControllerPort =
                aggregationControllerContainer.getMappedPort(AggregationController.HTTP_PORT);

        // when
        ResponseEntity<String> authenticateEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/authenticate",
                                aggregationHost, aggregationPort),
                        requestBodyForAuthenticateEndpoint);

        Optional<String> finalStatusForCredentials =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        String.format(
                                "http://%s:%d/data",
                                aggregationControllerHost, aggregationControllerPort),
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
        int aggregationPort = aggregationContainer.getMappedPort(Aggregation.HTTP_PORT);

        String aggregationControllerHost = aggregationControllerContainer.getContainerIpAddress();
        int aggregationControllerPort =
                aggregationControllerContainer.getMappedPort(AggregationController.HTTP_PORT);

        String aggregationControllerEndpoint =
                String.format(
                        "http://%s:%d/data", aggregationControllerHost, aggregationControllerPort);

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
    public void getAuthenticateForBarclaysShouldSetCredentialsStatusUpdated() throws Exception {
        // given
        String requestBodyForAuthenticateEndpoint =
                readRequestBodyFromFile(
                        "data/agents/uk/barclays/system_test_authenticate_request_body.json");
        String aggregationHost = aggregationContainer.getContainerIpAddress();
        int aggregationPort = aggregationContainer.getMappedPort(Aggregation.HTTP_PORT);

        String aggregationControllerHost = aggregationControllerContainer.getContainerIpAddress();
        int aggregationControllerPort =
                aggregationControllerContainer.getMappedPort(AggregationController.HTTP_PORT);

        // when
        ResponseEntity<String> authenticateEndpointCallResult =
                makePostRequest(
                        String.format(
                                "http://%s:%d/aggregation/authenticate",
                                aggregationHost, aggregationPort),
                        requestBodyForAuthenticateEndpoint);

        Optional<String> finalStatusForCredentials =
                pollForFinalCredentialsUpdateStatusUntilFlowEnds(
                        String.format(
                                "http://%s:%d/data",
                                aggregationControllerHost, aggregationControllerPort),
                        50,
                        1);

        // then
        Assert.assertEquals(204, authenticateEndpointCallResult.getStatusCodeValue());
        Assert.assertTrue(finalStatusForCredentials.isPresent());
        Assert.assertEquals("UPDATED", finalStatusForCredentials.get());
    }

    @After
    public void teardown() throws IOException {
        try {
            aggregationContainer.stop();
            aggregationControllerContainer.stop();

            // Just if we don't want to leave any traces behind
            client.removeImageCmd(Aggregation.IMAGE).exec();
            client.removeImageCmd(AggregationController.IMAGE).exec();
        } finally {
            client.close();
        }
    }
}
