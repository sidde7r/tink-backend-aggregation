package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment.Builder;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class CommerzbankPaymentMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/commerzbank/mock/resources/";

    private static final String CONFIGURATION_FILE = BASE_PATH + "configuration.yml";
    private static final String SINGLE_PAYMENT_ACCP_FILE =
            BASE_PATH + "commerzbank-single-payment_accp.aap";
    private static final String SINGLE_PAYMENT_TIMEOUT_FILE =
            BASE_PATH + "commerzbank-single-payment_timeout.aap";
    private static final String SINGLE_PAYMENT_DECOUPLED_FILE =
            BASE_PATH + "commerzbank-single-payment_decoupled-ok.aap";

    @Test
    public void testSinglePaymentAccepted() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Builder payment = createSinglePayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-commerzbank-ob", SINGLE_PAYMENT_ACCP_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(payment.build())
                        .addCallbackData("code", "87654321")
                        .buildWithoutLogin(PaymentCommand.class);

        // then
        assertThatCode(agentWireMockPaymentTest::executePayment).doesNotThrowAnyException();
    }

    @Test
    public void testSinglePaymentTimeout() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Builder payment = createSinglePayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-commerzbank-ob", SINGLE_PAYMENT_TIMEOUT_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(payment.build())
                        .withoutCallbackData()
                        .buildWithoutLogin(PaymentCommand.class);

        // when
        Throwable throwable = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(throwable).isInstanceOf(PaymentAuthorizationTimeOutException.class);
    }

    @Test
    public void testSinglePaymentDecoupled() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Builder payment = createSinglePayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-commerzbank-ob", SINGLE_PAYMENT_DECOUPLED_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(payment.build())
                        .addCallbackData("payment-confirmation", "woot")
                        .addCredentialField("username", "test_username")
                        .buildWithoutLogin(PaymentCommand.class);

        // when & then
        assertThatCode(agentWireMockPaymentTest::executePayment).doesNotThrowAnyException();
    }

    private Builder createSinglePayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("remittance information to creditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Creditor creditor =
                new Creditor(new IbanIdentifier("DE89370400440532013000"), "Creditor Name");
        Debtor debtor = new Debtor(new IbanIdentifier("DE27500105175141353468"));

        return new Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                .withCurrency("EUR")
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                .withPaymentServiceType(PaymentServiceType.SINGLE);
    }
}
