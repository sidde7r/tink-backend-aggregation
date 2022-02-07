package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.tsb.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class TsbAgentWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/ukob/tsb/wiremock/resources/";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";
    private static final String PROVIDER_NAME = "uk-tsb-oauth2";

    @Test
    public void testSuccessfulPayment() throws Exception {
        final String wireMockFilePath = RESOURCES_PATH + "payment_successful_case_mock_log.aap";
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithAuthCodeCallbackData(wireMockFilePath);

        agentWireMockPaymentTest.executePayment();
    }

    private AgentWireMockPaymentTest createAgentWireMockPaymentTestWithAuthCodeCallbackData(
            String wireMockFilePath) throws Exception {
        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, wireMockFilePath)
                .withConfigurationFile(readAgentConfiguration())
                .addCallbackData("code", "DUMMY_AUTH_CODE")
                .withHttpDebugTrace()
                .withPayment(createDomesticPayment())
                .buildWithoutLogin(PaymentCommand.class);
    }

    private AgentsServiceConfiguration readAgentConfiguration() throws Exception {
        return AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }

    private Payment createDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("166.00", "GBP");
        String currency = "GBP";
        String DESTINATION_IDENTIFIER = "12345612345678";
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.REFERENCE);
        remittanceInformation.setValue("WEALTHIFYJEANPN");
        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.SORT_CODE, DESTINATION_IDENTIFIER),
                                "Dummy Name"))
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId("39d4319a6b4c46d3a9c227966a10ba57")
                .build();
    }
}
