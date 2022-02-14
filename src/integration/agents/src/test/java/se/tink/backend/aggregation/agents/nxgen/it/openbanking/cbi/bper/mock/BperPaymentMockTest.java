package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.bper.mock;

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

public class BperPaymentMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/cbi/bper/mock/resources/";

    private static final String CONFIGURATION_FILE = BASE_PATH + "configuration.yml";
    private static final String WIREMOCK_FILE = BASE_PATH + "bper-recurring-payment_acsp.aap";

    @Test
    public void testRecurringPayment() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Payment.Builder payment = createAnyPayment();
        Payment recurringPayment = createAnyRecurringPayment(payment);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.IT, "it-bper-oauth2", WIREMOCK_FILE)
                        .withConfigurationFile(configuration)
                        .withPayment(recurringPayment)
                        .addCallbackData("state", "00000000-0000-4000-0000-000000000000")
                        .addCallbackData("result", "success")
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

    private Payment.Builder createAnyPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Bper");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Creditor creditor =
                new Creditor(new IbanIdentifier("IT45H0300203280271332616346"), "Creditor Name");
        Debtor debtor = new Debtor(new IbanIdentifier("IT29D0300203280625969137225"));

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                .withCurrency("EUR")
                .withRemittanceInformation(remittanceInformation);
    }
}
