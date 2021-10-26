package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.junit.Assert;
import se.tink.backend.aggregation.agents.contractproducer.ContractProducer;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module.AgentWiremockTestContextModule;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module.RefreshRequestModule;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.AgentWireMockRefreshTestNxBuilder;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step.MarketCodeStep;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.module.AgentFactoryWireMockModule;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.MutableFakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.RequestResponseParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventAccumulator;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.enums.MarketCode;

/**
 * It is recommended to use {@link AgentWireMockRefreshTest#nxBuilder()} method to create the object
 *
 * @see AgentWireMockRefreshTest
 */
@Slf4j
@SuppressWarnings("java:S2187")
public final class AgentWireMockRefreshTest {

    private final CompositeAgentTest compositeAgentTest;

    private final WireMockTestServer server;

    private final boolean dumpContentForContractFile;

    private final RawBankDataEventAccumulator rawBankDataEventAccumulator;

    public AgentWireMockRefreshTest(
            MarketCode marketCode,
            String providerName,
            Set<String> wireMockFilePaths,
            AgentsServiceConfiguration configuration,
            Map<String, String> loginDetails,
            String credentialPayload,
            Map<String, String> callbackData,
            Map<String, String> persistentStorageData,
            Map<String, String> sessionStorageData,
            Map<String, String> cache,
            TestModule agentTestModule,
            Set<RefreshableItem> refreshableItems,
            List<Class<? extends CompositeAgentTestCommand>> commandSequence,
            boolean httpDebugTrace,
            boolean dumpContentForContractFile,
            boolean requestFlagManual,
            boolean requestFlagCreate,
            boolean requestFlagUpdate,
            boolean wireMockServerLogsEnabled,
            boolean forceAutoAuthentication,
            UserAvailability userAvailability) {

        ImmutableSet<RequestResponseParser> parsers =
                wireMockFilePaths.stream()
                        .map(
                                wireMockFilePath ->
                                        new AapFileParser(
                                                ResourceFileReader.read(wireMockFilePath)))
                        .collect(ImmutableSet.toImmutableSet());

        server = new WireMockTestServer(parsers, wireMockServerLogsEnabled);
        rawBankDataEventAccumulator = new RawBankDataEventAccumulator();

        final Set<Module> modules =
                ImmutableSet.of(
                        new AgentWiremockTestContextModule(
                                marketCode,
                                providerName,
                                configuration,
                                loginDetails,
                                credentialPayload,
                                callbackData,
                                persistentStorageData,
                                sessionStorageData,
                                cache,
                                httpDebugTrace,
                                rawBankDataEventAccumulator),
                        new RefreshRequestModule(
                                refreshableItems,
                                requestFlagManual,
                                requestFlagCreate,
                                requestFlagUpdate,
                                forceAutoAuthentication,
                                userAvailability),
                        new AgentFactoryWireMockModule(
                                MutableFakeBankSocket.of(
                                        "localhost:" + server.getHttpPort(),
                                        "localhost:" + server.getHttpsPort()),
                                callbackData,
                                agentTestModule,
                                commandSequence));

        Injector injector = Guice.createInjector(modules);
        compositeAgentTest = injector.getInstance(CompositeAgentTest.class);
        this.dumpContentForContractFile = dumpContentForContractFile;
    }

    /**
     * Execute agent operations involved in WireMock test.
     *
     * @throws Exception May throw any exception that the agent throws.
     */
    public void executeRefresh() throws Exception {

        compositeAgentTest.executeCommands();
        if (server.hadEncounteredAnError()) {

            throw new RuntimeException(server.createErrorLogForFailedRequest());
        }
        if (dumpContentForContractFile) {
            ContractProducer contractProducer = new ContractProducer();
            log.info(
                    "This is the content for building the contract file : \n"
                            + contractProducer.produceFromContext(compositeAgentTest.getContext()));
        }
    }

    /** @return The state of Wiremock server or Optional.empty() if state is not set */
    public Optional<String> getCurrentState() {
        return server.getCurrentState();
    }

    public List<RawBankDataTrackerEvent> getEmittedRawBankDataEvents() {
        return rawBankDataEventAccumulator.getEventList();
    }

    /**
     * Assert that data fetched by agent matches the data provided in the AgentContractEntity.
     *
     * @param expected Data to be matched with data fetched by agent.
     */
    public void assertExpectedData(final AgentContractEntity expected) {

        final NewAgentTestContext context = compositeAgentTest.getContext();

        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expected.getIdentityData()
                                .map(Collections::singletonList)
                                .orElseGet(Collections::emptyList),
                        context.getIdentityData()
                                .map(Collections::singletonList)
                                .orElseGet(Collections::emptyList)));
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        ListUtils.emptyIfNull(expected.getAccounts()),
                        ListUtils.emptyIfNull(context.getUpdatedAccounts())));
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        ListUtils.emptyIfNull(expected.getTransactions()),
                        ListUtils.emptyIfNull(context.getTransactions())));

        if (CollectionUtils.isNotEmpty(expected.getTransferDestinationPatterns())
                && CollectionUtils.isNotEmpty(context.getTransferDestinationPatterns()))
            Assert.assertTrue(
                    AgentContractEntitiesAsserts.areListsMatchingVerbose(
                            ListUtils.emptyIfNull(expected.getTransferDestinationPatterns()),
                            ListUtils.emptyIfNull(context.getTransferDestinationPatterns())));
    }

    /**
     * @deprecated Consider using nxBuilder() instead - it adds ability to test authentication flow
     *     without data fetch
     */
    @Deprecated
    public static AgentWireMockRefreshTestBuilder builder(
            MarketCode market, String providerName, String wireMockFilePath) {
        return new AgentWireMockRefreshTestBuilder(market, providerName, wireMockFilePath);
    }

    /** Next gen step builder which adds ability to test authentication flow without data fetch */
    public static MarketCodeStep nxBuilder() {
        return new AgentWireMockRefreshTestNxBuilder();
    }
}
