package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bnpparibas.integration;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bnpparibas.integration.module.BnpParibasWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class BnpParibasWireMockTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/bnpparibas/integration/resources/configuration.yml";

    @Test
    public void test() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/bnpparibas/integration/resources/bnp_mock_log.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/bnpparibas/integration/resources/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.FR, "fr-bnpparibas-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .withAgentModule(new BnpParibasWireMockTestModule())
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testPayment() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/bnpparibas/integration/resources/bnp_payment_mock_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.FR, "fr-bnpparibas-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(
                                createMockedDomesticPayment(PaymentScheme.SEPA_CREDIT_TRANSFER))
                        .withAgentModule(new BnpParibasWireMockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        // when / then (execution and assertion currently done in the same step)
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testInstantPayment() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/bnpparibas/integration/resources/bnp_payment_Instant_mock_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.FR, "fr-bnpparibas-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(
                                createMockedDomesticPayment(
                                        PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER))
                        .withAgentModule(new BnpParibasWireMockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        // when / then (execution and assertion currently done in the same step)
        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment(PaymentScheme paymentScheme) {
        String currency = "EUR";
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", currency);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("test");

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, "FR1420041010050500013M02606"),
                                "Tink"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, "FR1261401750597365134612940")))
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId("paymentId")
                .withPaymentScheme(paymentScheme)
                .build();
    }
}
