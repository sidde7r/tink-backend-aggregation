package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.tesco.wiremock;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class TescoPaymentWiremockTest {
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/ukob/tesco/wiremock/resources/";
    private static final String CONFIGURATION_PATH = "configuration.yml";

    @Test
    public void testPayment() throws Exception {

        final String wireMockFilePath = RESOURCES_PATH + "payment_successful_case_mock_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(RESOURCES_PATH + CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.UK, "uk-tesco-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .withPayment(createMockedDomesticPayment())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("250.00", "GBP");
        LocalDate executionDate = LocalDate.now();
        String currency = "GBP";
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.REFERENCE);
        remittanceInformation.setValue("WEALTHIFYEWPJJE");
        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                new SortCodeIdentifier("12345612345678"), "Winterflood Securities"))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId("627b36196ce0453e8a7420730edea287")
                .build();
    }
}
