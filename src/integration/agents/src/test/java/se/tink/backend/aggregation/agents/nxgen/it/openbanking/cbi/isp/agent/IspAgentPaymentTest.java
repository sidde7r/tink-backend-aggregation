package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.isp.agent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.CreditorDebtorArgumentEnum;
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

public class IspAgentPaymentTest {
    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<CreditorDebtorArgumentEnum> creditorDebtorManager =
            new ArgumentManager<>(CreditorDebtorArgumentEnum.values());

    @Before
    public void setup() throws Exception {
        creditorDebtorManager.before();
        builder =
                new AgentIntegrationTest.Builder("it", "it-isp-oauth2")
                        .setFinancialInstitutionId("cbi")
                        .setAppId("tink")
                        .expectLoggedIn(false)
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

    @Test
    public void testInstantPayments() throws Exception {
        builder.build()
                .testTinkLinkPayment(
                        createRealDomesticPayment()
                                .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                                .build());
    }

    private Payment.Builder createRealDomesticRecurringPayment() {
        Payment.Builder recurringPayment = createRealDomesticPayment();
        recurringPayment.withPaymentServiceType(PaymentServiceType.PERIODIC);
        recurringPayment.withFrequency(Frequency.MONTHLY);
        recurringPayment.withStartDate(LocalDate.now().plusDays(2));
        recurringPayment.withEndDate(LocalDate.now().plusMonths(3));
        recurringPayment.withExecutionRule(ExecutionRule.PRECEDING);

        return recurringPayment;
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(CreditorDebtorArgumentEnum.CREDITOR));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");
        remittanceInformation.setValue("Tink testing " + LocalDateTime.now());
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(CreditorDebtorArgumentEnum.DEBTOR));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        LocalDate executionDate = LocalDate.now();

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(amount.getCurrencyCode())
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                .withPaymentServiceType(PaymentServiceType.SINGLE);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
