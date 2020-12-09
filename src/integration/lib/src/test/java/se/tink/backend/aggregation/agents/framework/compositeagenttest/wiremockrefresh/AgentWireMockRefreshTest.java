package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh;

import static com.google.common.collect.ImmutableList.of;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.ListUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contractproducer.ContractProducer;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module.AgentWiremockTestContextModule;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module.RefreshRequestModule;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.command.LoginCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.command.RefreshCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.module.AgentFactoryWireMockModule;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.MutableFakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.RequestResponseParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public final class AgentWireMockRefreshTest {

    private static final Logger log = LoggerFactory.getLogger(AgentWireMockRefreshTest.class);

    private final CompositeAgentTest compositeAgentTest;
    private final WireMockTestServer server;
    private final boolean dumpContentForContractFile;

    private AgentWireMockRefreshTest(
            MarketCode marketCode,
            String providerName,
            Set<String> wireMockFilePaths,
            AgentsServiceConfiguration configuration,
            Map<String, String> loginDetails,
            String credentialPayload,
            Map<String, String> callbackData,
            Map<String, String> persistentStorageData,
            TestModule agentTestModule,
            Set<RefreshableItem> refreshableItems,
            List<Class<? extends CompositeAgentTestCommand>> commandSequence,
            boolean httpDebugTrace,
            boolean dumpContentForContractFile,
            boolean requestFlagManual,
            boolean requestFlagCreate,
            boolean requestFlagUpdate) {

        ImmutableSet<RequestResponseParser> parsers =
                wireMockFilePaths.stream()
                        .map(
                                wireMockFilePath ->
                                        new AapFileParser(
                                                new ResourceFileReader().read(wireMockFilePath)))
                        .collect(ImmutableSet.toImmutableSet());

        server = new WireMockTestServer(parsers);

        final Set<Module> modules =
                ImmutableSet.of(
                        new AgentWiremockTestContextModule(
                                marketCode,
                                providerName,
                                configuration,
                                loginDetails,
                                credentialPayload,
                                callbackData,
                                persistentStorageData),
                        new RefreshRequestModule(
                                refreshableItems,
                                requestFlagManual,
                                requestFlagCreate,
                                requestFlagUpdate),
                        new AgentFactoryWireMockModule(
                                MutableFakeBankSocket.of("localhost:" + server.getHttpsPort()),
                                callbackData,
                                agentTestModule,
                                commandSequence,
                                httpDebugTrace));

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
        try {
            compositeAgentTest.execute();
        } catch (Exception e) {
            if (server.hadEncounteredAnError()) {
                throw new RuntimeException(server.createErrorLogForFailedRequest());
            }
            throw e;
        }
        if (dumpContentForContractFile) {
            ContractProducer contractProducer = new ContractProducer();
            log.info(
                    "This is the content for building the contract file : \n"
                            + contractProducer.produceFromContext(compositeAgentTest.getContext()));
        }
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
    }

    /**
     * Construct builder for creating an AgentWireMockRefreshTest.
     *
     * @param market MarketCode for provider to test.
     * @param providerName Provider name as specified in provider configuration.
     * @param wireMockFilePath Path to WireMock server instruction file.
     * @return A builder for AgentWireMockRefreshTest.
     */
    public static Builder builder(MarketCode market, String providerName, String wireMockFilePath) {
        return new Builder(market, providerName, wireMockFilePath);
    }

    public static class Builder {

        private final MarketCode market;
        private final String providerName;
        private final Set<String> wireMockFilePaths;
        private final Map<String, String> credentialFields;
        private String credentialPayload;
        private final Map<String, String> callbackData;
        private final Map<String, String> persistentStorageData;
        private final Set<RefreshableItem> refreshableItems;
        private boolean httpDebugTrace = false;
        private boolean dumpContentForContractFile = false;
        private boolean requestManual = true;
        private boolean requestCreate = false;
        private boolean requestUpdate = false;

        private AgentsServiceConfiguration configuration;
        private TestModule agentTestModule;

        private Builder(MarketCode market, String providerName, String wireMockFilePath) {
            this.market = market;
            this.providerName = providerName;
            this.wireMockFilePaths = new HashSet<>(Collections.singleton(wireMockFilePath));
            this.configuration = new AgentsServiceConfiguration();
            this.credentialFields = new HashMap<>();
            this.callbackData = new HashMap<>();
            this.persistentStorageData = new HashMap<>();
            this.refreshableItems = new HashSet<>();
        }

        /**
         * Use specified AgentsServiceConfiguration for agent. Agent will get an empty configuration
         * if none is specified.
         *
         * @param configuration Configuration to use for agent.
         * @return This builder.
         */
        public Builder withConfigurationFile(AgentsServiceConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        /**
         * Add credential field. e.g. username or password.
         *
         * <p>Can be called multiple times to add several items.
         *
         * @param key Key of credential field.
         * @param value Value of credential field.
         * @return This builder.
         */
        public Builder addCredentialField(String key, String value) {
            credentialFields.put(key, value);
            return this;
        }

        public Builder addCredentialPayload(String credentialPayload) {
            this.credentialPayload = credentialPayload;
            return this;
        }

        /**
         * Add callback data to be returned from supplemental information request. TODO: This should
         * be moved to more flexible configuration file.
         *
         * <p>Can be called multiple times to add several items.
         *
         * @param key Key of callback data.
         * @param value Value of callback data.
         * @return This builder.
         */
        public Builder addCallbackData(String key, String value) {
            callbackData.put(key, value);
            return this;
        }

        /**
         * Add data to be added to persistent storage. TODO: This should be moved to more flexible
         * configuration file.
         *
         * <p>Can be called multiple times to add several items.
         *
         * @param key Key of data to put to persistent storage.
         * @param value Value of data to put to persistent storage.
         * @return This builder.
         */
        public Builder addPersistentStorageData(String key, String value) {
            persistentStorageData.put(key, value);
            return this;
        }

        /**
         * Allows adding additional dependencies via Guice dependency injection to agent.
         * Dependencies bound in the provided module will be available in the agents constructor,
         * typically this should be used to bind fake/mock versions of dependencies used in
         * production.
         *
         * @param module Guice module to bind.
         * @return This builder.
         */
        public Builder withAgentModule(TestModule module) {
            this.agentTestModule = module;
            return this;
        }

        /**
         * Add refreshable items. If not specified agent will use <code>
         * RefreshableItem.REFRESHABLE_ITEMS_ALL</code>.
         *
         * <p>Can be called multiple times to add several items.
         *
         * @param items Items to refresh.
         * @return This builder.
         */
        public Builder addRefreshableItems(RefreshableItem... items) {
            this.refreshableItems.addAll(Arrays.asList(items));
            return this;
        }

        /**
         * Add refreshable items. If not specified agent will use <code>
         * RefreshableItem.REFRESHABLE_ITEMS_ALL</code>.
         *
         * <p>Can be called multiple times to add several items.
         *
         * @param items Items to refresh.
         * @return This builder.
         */
        public Builder addRefreshableItems(Set<RefreshableItem> items) {
            this.refreshableItems.addAll(items);
            return this;
        }

        /**
         * Enables http debug trace printout
         *
         * @return This builder.
         */
        public Builder withHttpDebugTrace() {
            this.httpDebugTrace = true;
            return this;
        }

        /**
         * Enables writing content for building a contract file at the end
         *
         * @return This builder.
         */
        public Builder dumpContentForContractFile() {
            this.dumpContentForContractFile = true;
            return this;
        }

        /**
         * Add AAP file to be used.
         *
         * <p>Can be called multiple times to add several files.
         *
         * @param wireMockFilePath AAP file to be used.
         * @return This builder.
         */
        public Builder addAnotherWireMockFile(String wireMockFilePath) {
            this.wireMockFilePaths.add(wireMockFilePath);
            return this;
        }

        /**
         * Set the manual flag on the credentials request.
         *
         * @param manual Value for the request's manual flag.
         * @return This builder.
         */
        public Builder withRequestFlagManual(boolean manual) {
            this.requestManual = manual;
            return this;
        }

        /**
         * Set the create flag on the credentials request.
         *
         * @param create Value for the request's create flag.
         * @return This builder.
         */
        public Builder withRequestFlagCreate(boolean create) {
            this.requestCreate = create;
            return this;
        }

        /**
         * Set the update flag on the credentials request.
         *
         * @param update Value for the request's update flag.
         * @return This builder.
         */
        public Builder withRequestFlagUpdate(boolean update) {
            this.requestUpdate = update;
            return this;
        }

        public AgentWireMockRefreshTest build() {
            if (refreshableItems.isEmpty()) {
                refreshableItems.addAll(
                        RefreshableItem.sort(RefreshableItem.REFRESHABLE_ITEMS_ALL));
            }

            return new AgentWireMockRefreshTest(
                    market,
                    providerName,
                    wireMockFilePaths,
                    configuration,
                    credentialFields,
                    credentialPayload,
                    callbackData,
                    persistentStorageData,
                    agentTestModule,
                    refreshableItems,
                    of(LoginCommand.class, RefreshCommand.class),
                    httpDebugTrace,
                    dumpContentForContractFile,
                    requestManual,
                    requestCreate,
                    requestUpdate);
        }
    }
}
