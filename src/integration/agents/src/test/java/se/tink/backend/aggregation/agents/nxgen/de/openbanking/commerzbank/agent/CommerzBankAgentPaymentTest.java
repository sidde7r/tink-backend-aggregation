package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.agent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
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

public class CommerzBankAgentPaymentTest {

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT,
        CREDITOR_ACCOUNT;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<ArgumentManager.UsernameArgumentEnum> usernameManager =
            new ArgumentManager<>(ArgumentManager.UsernameArgumentEnum.values());
    private final ArgumentManager<CommerzBankAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(CommerzBankAgentPaymentTest.Arg.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        usernameManager.before();
        creditorDebtorManager.before();

        builder =
                new AgentIntegrationTest.Builder("de", "de-commerzbank-ob")
                        .setFinancialInstitutionId("fadaee91c1f3453eb844c0b9e7a77c81")
                        .addCredentialField(
                                Key.USERNAME,
                                usernameManager.get(ArgumentManager.UsernameArgumentEnum.USERNAME))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setAppId("tink");
    }

    @Test
    public void testSepaPayments() throws Exception {
        builder.build()
                .testTinkLinkPayment(
                        createSepaPayment().withExecutionDate(LocalDate.now().plusDays(1)).build());
    }

    @Test
    public void testSepaInstantPayments() throws Exception {
        builder.build().testTinkLinkPayment(createSepaInstantPayment().build());
    }

    @Test
    public void testRecurringPayments() throws Exception {
        usernameManager.before();
        creditorDebtorManager.before();

        builder.build().testTinkLinkPayment(createRecurringPayment().build());
    }

    private Payment.Builder createRecurringPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(
                "RecurringPaymentReferenceToCreditor "
                        + LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd 'at' HH:mm")));
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        return createSepaPayment()
                .withRemittanceInformation(remittanceInformation)
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withFrequency(Frequency.MONTHLY)
                .withStartDate(LocalDate.now().plusDays(2))
                .withEndDate(LocalDate.now().plusMonths(2))
                .withExecutionRule(ExecutionRule.FOLLOWING)
                .withDayOfMonth(10);
    }

    private Payment.Builder createSepaPayment() {
        return createRealDomesticPayment().withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER);
    }

    private Payment.Builder createSepaInstantPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(
                "SepaInstantReferenceToCreditor "
                        + LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd 'at' HH:mm")));
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        return createRealDomesticPayment()
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER);
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(
                "SepaReferenceToCreditor "
                        + LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd 'at' HH:mm")));
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(
                                CommerzBankAgentPaymentTest.Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(CommerzBankAgentPaymentTest.Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        String currency = "EUR";

        return new Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withPaymentServiceType(PaymentServiceType.SINGLE);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
