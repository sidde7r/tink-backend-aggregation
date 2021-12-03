package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import org.junit.Test;
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

public class SparkassenPaymentMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/sparkassen/mock/resources/";
    private static final String CONFIGURATION_FILE = BASE_PATH + "configuration.yml";

    private static final String SINGLE_PAYMENT_REJECTED_FILE = BASE_PATH + "payment_rejected.aap";

    @Test
    public void testSinglePaymentAccepted() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Builder payment = createSinglePayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE,
                                "de-sparkassestadmünchen-ob",
                                SINGLE_PAYMENT_REJECTED_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(payment.build())
                        .addCredentialField("username", "username")
                        .addCredentialField("password", "password")
                        .addCallbackData("pushTan", "123456")
                        .buildWithoutLogin(PaymentCommand.class);

        // when
        Throwable throwable = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(throwable).isInstanceOf(PaymentRejectedException.class);
    }

    private Builder createSinglePayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("remittance information to creditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Creditor creditor =
                new Creditor(new IbanIdentifier("DE27500105175141353468"), "Creditor Name");
        Debtor debtor = new Debtor(new IbanIdentifier("DE23500105171883783625"));

        return new Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                .withCurrency("EUR")
                .withRemittanceInformation(remittanceInformation);
    }
}
