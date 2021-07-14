package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.mock.pis;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.Test;
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
import se.tink.libraries.payment.rpc.Payment.Builder;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DkbWiremockPaymentAgentTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/dkb/mock/pis/resources/";

    private static final String CONFIGURATION_FILE = BASE_PATH + "configuration.yml";
    private static final String WIREMOCK_FILE = BASE_PATH + "recurring-payment.aap";

    @Test
    public void testRecurringPayment() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Builder payment = createAnyPayment();
        Payment recurringPayment = createAnyRecurringPayment(payment);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.DE, "de-dkb-ob", WIREMOCK_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(recurringPayment)
                        .addCredentialField("username", "username")
                        .addCredentialField("password", "password")
                        .addCallbackData("pushTan", "123456")
                        .buildWithoutLogin(PaymentCommand.class);

        // then
        Assertions.assertThatCode(agentWireMockPaymentTest::executePayment)
                .doesNotThrowAnyException();
    }

    private Payment createAnyRecurringPayment(Builder payment) {
        LocalDate startDate = LocalDate.of(2020, 10, 15);

        return payment.withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withFrequency(Frequency.MONTHLY)
                .withDayOfMonth(20)
                .withStartDate(startDate)
                .withEndDate(startDate.plusMonths(2))
                .withExecutionRule(ExecutionRule.FOLLOWING)
                .build();
    }

    private Builder createAnyPayment() {
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
