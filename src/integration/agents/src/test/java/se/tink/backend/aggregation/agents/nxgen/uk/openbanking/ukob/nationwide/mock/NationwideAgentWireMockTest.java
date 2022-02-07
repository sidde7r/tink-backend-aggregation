package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.nationwide.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
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

public class NationwideAgentWireMockTest {
    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/ukob/nationwide/mock/resources/configuration.yml";

    @Test
    public void testPayment() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/ukob/nationwide/mock/resources/executed_domestic_payment.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.UK, "uk-nationwide-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .withPayment(
                                createMockedDomesticPayment()) // Keep me and remove the line above
                        .withHttpDebugTrace()
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testCancelPayment() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/ukob/nationwide/mock/resources/cancelled_domestic_payment.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.UK, "uk-nationwide-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addCallbackData("error", "access_denied")
                        .addCallbackData("error_description", "access_denied")
                        .withPayment(
                                createMockedDomesticPayment()) // Keep me and remove the line above
                        .withHttpDebugTrace()
                        .buildWithoutLogin(PaymentCommand.class);

        Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationCancelledByUserException.class)
                .hasNoCause()
                .hasMessage("Authorisation of payment was cancelled. Please try again.");
    }

    private Payment createMockedDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("10.00", "GBP");
        String currency = "GBP";
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.REFERENCE);
        remittanceInformation.setValue("Donation");
        return new Payment.Builder()
                .withCreditor(new Creditor(new SortCodeIdentifier("12345612345678"), "Amnesty"))
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId("c6aef91a03e44b34b355422d142fa3ff")
                .build();
    }
}
