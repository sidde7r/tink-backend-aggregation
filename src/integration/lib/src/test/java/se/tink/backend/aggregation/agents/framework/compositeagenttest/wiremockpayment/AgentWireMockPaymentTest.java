package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment;

import static com.google.common.collect.ImmutableList.of;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module.AgentWiremockTestContextModule;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module.RefreshRequestModule;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.command.LoginCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentGBCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module.AgentFactoryWireMockModule;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module.PaymentRequestModule;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module.VerdictModule;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.MutableFakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.Transfer;

public final class AgentWireMockPaymentTest {

    private final CompositeAgentTest compositeAgentTest;
    private final WireMockTestServer server;

    private AgentWireMockPaymentTest(
            MarketCode marketCode,
            String providerName,
            String wireMockFilePath,
            AgentsServiceConfiguration configuration,
            Map<String, String> loginDetails,
            Map<String, String> callbackData,
            TestModule agentModule,
            List<Payment> paymentList,
            List<Transfer> transfersList,
            List<Class<? extends CompositeAgentTestCommand>> commandSequence,
            boolean httpDebugTrace) {

        server =
                new WireMockTestServer(
                        ImmutableSet.of(
                                new AapFileParser(
                                        new ResourceFileReader().read(wireMockFilePath))));

        /*
        TODO: For now null value for "supplementalInfoForCredentials" seem fine, let's wait
        for other agents to see if this value should be injected at all or not
         */
        final Set<Module> modules =
                ImmutableSet.of(
                        new AgentWiremockTestContextModule(
                                marketCode,
                                providerName,
                                configuration,
                                loginDetails,
                                null,
                                callbackData),
                        new RefreshRequestModule(
                                RefreshableItem.REFRESHABLE_ITEMS_ALL, true, false, false),
                        new PaymentRequestModule(paymentList, transfersList),
                        new VerdictModule(),
                        new AgentFactoryWireMockModule(
                                MutableFakeBankSocket.of("localhost:" + server.getHttpsPort()),
                                callbackData,
                                agentModule,
                                commandSequence,
                                httpDebugTrace));

        Injector injector = Guice.createInjector(modules);
        compositeAgentTest = injector.getInstance(CompositeAgentTest.class);
    }

    /**
     * Execute agent operations involved in WireMock test.
     *
     * @throws Exception May throw any exception that the agent throws.
     */
    public void executePayment() throws Exception {
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
     * Construct builder for creating an AgentWireMockPaymentTest.
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
        private final List<Payment> paymentList;
        private final List<Transfer> transferList;
        private boolean httpDebugTrace = false;

        private AgentsServiceConfiguration configuration;
        private TestModule agentTestModule;

        private Builder(MarketCode market, String providerName, String wireMockFilePath) {
            this.market = market;
            this.providerName = providerName;
            this.wireMockFilePath = wireMockFilePath;
            this.configuration = new AgentsServiceConfiguration();
            this.credentialFields = new HashMap<>();
            this.callbackData = new HashMap<>();
            this.paymentList = new ArrayList<>();
            this.transferList = new ArrayList<>();
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
        public Builder withAgentModule(TestModule module) {
            this.agentTestModule = module;
            return this;
        }

        /**
         * Add payment to be executed by the agent.
         *
         * <p>Can be called multiple times to add several items.
         *
         * @param payment Payment request
         * @return This builder.
         */
        public Builder addPayment(Payment payment) {
            this.paymentList.add(payment);
            return this;
        }

        public Builder addTransfer(Transfer transfer) {
            this.transferList.add(transfer);
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
         * Builds payment test that does not attempt to login before executing payment.
         *
         * @return WireMock payment test.
         */
        public AgentWireMockPaymentTest buildWithoutLogin() {

            return new AgentWireMockPaymentTest(
                    market,
                    providerName,
                    wireMockFilePath,
                    configuration,
                    credentialFields,
                    callbackData,
                    agentTestModule,
                    paymentList,
                    transferList,
                    of(PaymentCommand.class),
                    httpDebugTrace);
        }

        /**
         * Builds payment test that does not attempt to login before executing payment.
         *
         * @return WireMock payment test.
         */
        public AgentWireMockPaymentTest buildWithoutLoginWithGBCommand() {

            return new AgentWireMockPaymentTest(
                    market,
                    providerName,
                    wireMockFilePath,
                    configuration,
                    credentialFields,
                    callbackData,
                    agentTestModule,
                    paymentList,
                    transferList,
                    of(PaymentGBCommand.class),
                    httpDebugTrace);
        }
        /**
         * Builds payment test that will login before executing the payment.
         *
         * @return WireMock payment test.
         */
        public AgentWireMockPaymentTest buildWithLogin() {

            return new AgentWireMockPaymentTest(
                    market,
                    providerName,
                    wireMockFilePath,
                    configuration,
                    credentialFields,
                    callbackData,
                    agentTestModule,
                    paymentList,
                    transferList,
                    of(LoginCommand.class, PaymentCommand.class),
                    httpDebugTrace);
        }
    }
}
