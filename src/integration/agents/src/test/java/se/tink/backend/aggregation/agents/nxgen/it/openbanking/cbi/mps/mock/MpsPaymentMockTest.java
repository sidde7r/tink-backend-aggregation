package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.mps.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
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

public class MpsPaymentMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/cbi/mps/mock/resources/";

    private static final String CONFIGURATION_FILE = BASE_PATH + "configuration.yml";
    private static final String SINGLE_PAYMENT_INITIATED_FILE =
            BASE_PATH + "mps-single-payment_initiated.aap";
    private static final String SINGLE_PAYMENT_UNKNOWN_FILE =
            BASE_PATH + "mps-single-payment_unknown.aap";

    @Test
    public void testSinglePaymentTimeout() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Builder payment = createSinglePayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.IT, "it-mps-oauth2", SINGLE_PAYMENT_INITIATED_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(payment.build())
                        .withoutCallbackData()
                        .buildWithoutLogin(PaymentCommand.class);

        // when
        Throwable throwable = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(throwable).isInstanceOf(PaymentAuthorizationException.class);
    }

    @Test
    public void testSinglePaymentUserCancelledLogin() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Builder payment = createSinglePayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.IT, "it-mps-oauth2", SINGLE_PAYMENT_UNKNOWN_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(payment.build())
                        .addCallbackData("state", "00000000-0000-4000-0000-000000000000")
                        .addCallbackData("result", "success")
                        .buildWithoutLogin(PaymentCommand.class);

        // when
        Throwable throwable = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(throwable).isInstanceOf(PaymentAuthorizationCancelledByUserException.class);
    }

    private Builder createSinglePayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("remittance information to creditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Creditor creditor =
                new Creditor(new IbanIdentifier("IT45H0300203280271332616346"), "Creditor Name");
        Debtor debtor = new Debtor(new IbanIdentifier("IT29D0300203280625969137225"));

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
