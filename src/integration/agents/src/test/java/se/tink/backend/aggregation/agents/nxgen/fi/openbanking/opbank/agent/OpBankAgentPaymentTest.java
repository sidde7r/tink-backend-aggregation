package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.agent;

import java.time.LocalDate;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentExecutor;
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

@Ignore
public class OpBankAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<ArgumentManager.PsuIdArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.PsuIdArgumentEnum.values());
    private final ArgumentManager<OpBankAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(OpBankAgentPaymentTest.Arg.values());

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fi", "fi-opbank-ob")
                        .setFinancialInstitutionId("opbank")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testSepaPayments() throws Exception {
        manager.before();
        creditorDebtorManager.before();

        builder.build()
                .testTinkLinkPayment(
                        createRealDomesticPayment()
                                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                                .build());
    }

    @Test
    public void testRecurringPayments() throws Exception {
        manager.before();
        creditorDebtorManager.before();

        builder.build()
                .testTinkLinkPayment(
                        createRealDomesticRecurringPayment()
                                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                                .build());
    }

    private Payment.Builder createRealDomesticRecurringPayment() {
        LocalDate startDate = BasePaymentExecutor.createStartDateForRecurringPayment(0);
        Payment.Builder recurringPayment = createRealDomesticPayment();
        recurringPayment.withPaymentServiceType(PaymentServiceType.PERIODIC);
        recurringPayment.withFrequency(Frequency.EVERY_TWO_WEEKS);
        recurringPayment.withStartDate(startDate);
        recurringPayment.withEndDate(startDate.plusMonths(1));
        recurringPayment.withExecutionRule(ExecutionRule.FOLLOWING);
        recurringPayment.withDayOfMonth(startDate.getDayOfMonth());

        return recurringPayment;
    }

    @Test
    public void testSepaInstantPayments() throws Exception {
        manager.before();
        creditorDebtorManager.before();

        builder.build()
                .testTinkLinkPayment(
                        createRealDomesticPayment()
                                .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                                .build());
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(OpBankAgentPaymentTest.Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "CreditorName");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Tink Test");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(OpBankAgentPaymentTest.Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(2.15);
        LocalDate executionDate = LocalDate.now();
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation);
    }

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT,
        CREDITOR_ACCOUNT,
        CREDITOR_NAME;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
