package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki.wiremock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.libraries.enums.MarketCode.FI;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SPankkiAgentPaymentWiremockTest {

    private static final String PROVIDER_NAME = "fi-spankki-ob";

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/spankki/wiremock/resources/configuration.yml";

    private static final String WIREMOCK_SERVER_FILEPATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/spankki/wiremock/resources/spankki_ob_wiremock_payment_success.aap";
    private static final String WIREMOCK_SERVER_FILEPATH_CANCELLED =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/spankki/wiremock/resources/spankki_ob_wiremock_payment_cancelled.aap";

    @Test
    public void testStandardPayment() throws Exception {

        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(FI, PROVIDER_NAME, WIREMOCK_SERVER_FILEPATH)
                        .withConfigurationFile(configuration)
                        .withPayment(createStandardPayment())
                        .withHttpDebugTrace()
                        .addCallbackData("code", "dummyCode")
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testCancelledPayment() throws Exception {

        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                FI, PROVIDER_NAME, WIREMOCK_SERVER_FILEPATH_CANCELLED)
                        .withConfigurationFile(configuration)
                        .withPayment(createStandardPayment())
                        .withHttpDebugTrace()
                        .addCallbackData("code", "dummyCode")
                        .addCallbackData("error", "access_denied")
                        .buildWithoutLogin(PaymentCommand.class);

        // when
        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown).isExactlyInstanceOf(PaymentAuthorizationException.class);
    }

    private Payment createStandardPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("610550873500157");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        // Ibans are randomly generated
        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("FI9731268662779291");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Tink Test");

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("FI9275931178545771");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(0.01);
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .build();
    }
}
