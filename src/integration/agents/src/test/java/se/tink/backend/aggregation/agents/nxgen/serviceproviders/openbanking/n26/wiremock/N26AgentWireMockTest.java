package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class N26AgentWireMockTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/n26/wiremock/resources/configuration.yml";
    private static final String WIREMOCK_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/n26/wiremock/resources/n26_mock_log.aap";
    private static final String CONTRACT_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/n26/wiremock/resources/agent-contract.json";

    @Test
    public void testRefresh() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(MarketCode.FR, "fr-n26-ob", WIREMOCK_FILE_PATH)
                        .withConfigurationFile(configuration)
                        .addCallbackData("tokenId", "DUMMY_ACCESS_TOKEN")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testPayment() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/n26/wiremock/resources/n26_mock_payment_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.FR, "fr-n26-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("tokenId", "DUMMY_TOKEN_ID")
                        .withPayment(createMockedDomesticPayment())
                        .buildWithoutLogin(PaymentCommand.class);

        // when / then (execution and assertion currently done in the same step)
        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment() {
        String currency = "EUR";
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", currency);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("test");

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                new IbanIdentifier("AGRIFRPP881", "FR1420041010050500013M02606"),
                                "Tink"))
                .withDebtor(
                        new Debtor(
                                new IbanIdentifier("AGRIFRPP881", "FR1261401750597365134612940")))
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId("paymentId")
                .build();
    }
}
