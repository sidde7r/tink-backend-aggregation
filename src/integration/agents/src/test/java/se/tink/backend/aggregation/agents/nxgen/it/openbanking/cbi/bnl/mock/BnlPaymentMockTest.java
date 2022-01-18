package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.bnl.mock;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
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

public class BnlPaymentMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/cbi/bnl/mock/resources/";

    private static final String CONFIGURATION_FILE = BASE_PATH + "configuration.yml";
    private static final String RECURRING_PAYMENT_ACCP_FILE =
            BASE_PATH + "bnl-recurring-payment_actc.aap";

    @Test
    public void testRecurringPayment() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Builder payment = createPayment();
        Payment recurringPayment = createRecurringPayment(payment);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.IT, "it-bnl-oauth2", RECURRING_PAYMENT_ACCP_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(recurringPayment)
                        .addCallbackData("state", "00000000-0000-4000-0000-000000000000")
                        .addCallbackData("result", "success")
                        .buildWithoutLogin(PaymentCommand.class);

        // then
        assertThatCode(agentWireMockPaymentTest::executePayment).doesNotThrowAnyException();
    }

    private Payment createRecurringPayment(Builder payment) {
        LocalDate startDate = LocalDate.of(2021, 8, 30);

        return payment.withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withFrequency(Frequency.MONTHLY)
                .withDayOfMonth(30)
                .withStartDate(startDate)
                .withEndDate(startDate.plusMonths(2))
                .withExecutionRule(ExecutionRule.FOLLOWING)
                .build();
    }

    private Builder createPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("remittance information to creditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Creditor creditor =
                new Creditor(new IbanIdentifier("IT52X0300203280728575573739"), "Creditor Name");
        Debtor debtor = new Debtor(new IbanIdentifier("IT53X0300203280882749129712"));

        return new Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                .withCurrency("EUR")
                .withRemittanceInformation(remittanceInformation);
    }
}
