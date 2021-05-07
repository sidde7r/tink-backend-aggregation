package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.agent;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class FinecoBankAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("it", "it-finecobank-oauth2")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("finecobank")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testTinkLinkPayment(createRealDomesticPayment().build());
    }

    @Test
    public void testRecurringPayments() throws Exception {
        builder.build().testTinkLinkPayment(createRealDomesticRecurringPayment().build());
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Remittance goes here");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("zxcv");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");
        AccountIdentifier debtorIdentifier = new IbanIdentifier("asdf");
        Debtor debtor = new Debtor(debtorIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        LocalDate executionDate = LocalDate.now().plusDays(1);
        String currency = "EUR";

        return new Payment.Builder()
                .withDebtor(debtor)
                .withCreditor(creditor)
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                .withPaymentServiceType(PaymentServiceType.SINGLE);
    }

    private Payment.Builder createRealDomesticRecurringPayment() {
        return createRealDomesticPayment()
                .withExecutionDate(null)
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withFrequency(Frequency.MONTHLY)
                .withStartDate(LocalDate.now().plusDays(2))
                .withEndDate(LocalDate.now().plusMonths(1).plusDays(4))
                .withExecutionRule(ExecutionRule.FOLLOWING);
    }
}
