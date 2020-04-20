package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module.AgentContextModule;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module.RefreshRequestModule;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.module.AgentFactoryWireMockModule;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.MutableFakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public final class AgentWireMockRefreshTest {

    private final CompositeAgentTest compositeAgentTest;
    private final WireMockTestServer server;

    private AgentWireMockRefreshTest(
            MarketCode marketCode,
            String providerName,
            String wireMockFilePath,
            AgentsServiceConfiguration configuration,
            Map<String, String> loginDetails,
            Map<String, String> callbackData,
            Module agentModule,
            Set<RefreshableItem> refreshableItems) {

        server = new WireMockTestServer();
        server.prepareMockServer(
                new AapFileParser(new ResourceFileReader().read(wireMockFilePath)));

        final Set<Module> modules =
                ImmutableSet.of(
                        new AgentContextModule(
                                marketCode, providerName, configuration, loginDetails),
                        new RefreshRequestModule(refreshableItems),
                        new AgentFactoryWireMockModule(
                                MutableFakeBankSocket.of("localhost:" + server.getHttpsPort()),
                                callbackData,
                                agentModule));

        Injector injector = Guice.createInjector(modules);
        compositeAgentTest = injector.getInstance(CompositeAgentTest.class);
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
                        expected.getAccounts(), context.getUpdatedAccounts()));
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expected.getTransactions(), context.getTransactions()));
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
        private final String wireMockFilePath;
        private final Map<String, String> credentialFields;
        private final Map<String, String> callbackData;
        private final Set<RefreshableItem> refreshableItems;

        private AgentsServiceConfiguration configuration;
        private Module agentModule;

        private Builder(MarketCode market, String providerName, String wireMockFilePath) {
            this.market = market;
            this.providerName = providerName;
            this.wireMockFilePath = wireMockFilePath;
            this.configuration = new AgentsServiceConfiguration();
            this.credentialFields = new HashMap<>();
            this.callbackData = new HashMap<>();
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
         * Allows adding additional dependencies via Guice dependency injection to agent.
         * Dependencies bound in the provided module will be available in the agents constructor,
         * typically this should be used to bind fake/mock versions of dependencies used in
         * production.
         *
         * @param module Guice module to bind.
         * @return This builder.
         */
        public Builder withAgentModule(Module module) {
            this.agentModule = module;
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

        public AgentWireMockRefreshTest build() {
            if (refreshableItems.isEmpty()) {
                refreshableItems.addAll(
                        RefreshableItem.sort(RefreshableItem.REFRESHABLE_ITEMS_ALL));
            }

            return new AgentWireMockRefreshTest(
                    market,
                    providerName,
                    wireMockFilePath,
                    configuration,
                    credentialFields,
                    callbackData,
                    agentModule,
                    refreshableItems);
        }
    }
}
