package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.bpm.mock;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class BpmPaymentMockTest {
    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/cbi/bpm/mock/resources/";
    private static final String CONFIGURATION_FILE = BASE_PATH + "configuration.yml";

    @Test
    public void testRecurringPaymentInitiation() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        final String wireMockFilePath = BASE_PATH + "recurringPayment.aap";

        LocalDate refDate = LocalDate.of(2021, 7, 10);
        // build recurring payment
        Payment payment = createRecurringPayment(refDate);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.IT, "it-bpm-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCredentialField("username", "dummy_psu_id")
                        .withPayment(payment)
                        .addCallbackData("state", "00000000-0000-4000-0000-000000000000")
                        .addCallbackData("result", "success")
                        .buildWithoutLogin(PaymentCommand.class);
        agentWireMockPaymentTest.executePayment();

        // then
        assertThatCode(agentWireMockPaymentTest::executePayment).doesNotThrowAnyException();
    }

    private Payment createRecurringPayment(LocalDate refDate) {
        return createPayment()
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withFrequency(Frequency.MONTHLY)
                .withDayOfMonth(refDate.getDayOfMonth())
                .withStartDate(refDate)
                .withEndDate(refDate.plusMonths(2))
                .withExecutionRule(ExecutionRule.FOLLOWING)
                .build();
    }

    private Payment.Builder createPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("SepaReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier("IT12X0542811101000700088930");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "dummy owner");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier("IT12L8551867857UFGAYZF25O4M");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation);
    }
}
