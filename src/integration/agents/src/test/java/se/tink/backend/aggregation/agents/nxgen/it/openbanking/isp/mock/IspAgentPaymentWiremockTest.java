package se.tink.backend.aggregation.agents.nxgen.it.openbanking.isp.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
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
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class IspAgentPaymentWiremockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/isp/mock/resources/";

    private static final String CONFIGURATION_FILE = BASE_PATH + "configuration.yml";
    private static final String SINGLE_PAYMENT_ACCP_FILE =
            BASE_PATH + "isp-single-payment_accp.aap";

    private static final String SINGLE_PAYMENT_RJCT_FILE =
            BASE_PATH + "isp-single-payment_rjct.aap";

    private static final String SINGLE_PAYMENT_TIMEOUT_FILE =
            BASE_PATH + "isp-single-payment_timeout.aap";

    @Test
    public void testSinglePaymentAccepted() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Builder payment = createSinglePayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.IT, "it-isp-oauth2", SINGLE_PAYMENT_ACCP_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(payment.build())
                        .addCallbackData("tpcb_00000000-0000-4000-0000-000000000000", "success")
                        .buildWithoutLogin(PaymentCommand.class);

        // then
        Assertions.assertThatCode(agentWireMockPaymentTest::executePayment)
                .doesNotThrowAnyException();
    }

    @Test
    public void testSinglePaymentRejected() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Builder payment = createSinglePayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.IT, "it-isp-oauth2", SINGLE_PAYMENT_RJCT_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(payment.build())
                        .addCallbackData("tpcb_00000000-0000-4000-0000-000000000000", "failure")
                        .buildWithoutLogin(PaymentCommand.class);

        // when
        Throwable throwable = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(throwable).isInstanceOf(PaymentRejectedException.class);
    }

    @Test
    public void testSinglePaymentTimeout() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Builder payment = createSinglePayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.IT, "it-isp-oauth2", SINGLE_PAYMENT_TIMEOUT_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(payment.build())
                        .buildWithoutLogin(PaymentCommand.class);

        // when
        Throwable throwable = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(throwable).isInstanceOf(PaymentException.class);
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
                .withRemittanceInformation(remittanceInformation);
    }
}
