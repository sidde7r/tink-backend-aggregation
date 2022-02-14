package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.ing.wiremock;

import io.dropwizard.configuration.ConfigurationException;
import java.io.IOException;
import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RunWith(JUnitParamsRunner.class)
public class IngPaymentWiremockTest {

    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/ing/wiremock/resources/";
    private static final String CONFIGURATION_PATH = RESOURCE_PATH + "configuration.yml";

    private final AgentsServiceConfiguration configuration = getConfiguration();

    private static AgentsServiceConfiguration getConfiguration() {
        try {
            return AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        } catch (IOException | ConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testInstantSepaPayment() throws Exception {
        // given
        final String wireMockFilePath = RESOURCE_PATH + "nl-ing-pis_instant_sepa.aap";

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.NL, "nl-ing-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(createMockedPayment())
                        .buildWithoutLogin(PaymentCommand.class);

        // expect
        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedPayment() {
        String currency = "EUR";
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.01", currency);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("test value");

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(new IbanIdentifier("NL98INGB1234567890"), "Payment Creditor"))
                .withDebtor(new Debtor(new IbanIdentifier("NL21INGB0987654321")))
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId("TEST_PAYMENT_ID")
                .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                .build();
    }
}
