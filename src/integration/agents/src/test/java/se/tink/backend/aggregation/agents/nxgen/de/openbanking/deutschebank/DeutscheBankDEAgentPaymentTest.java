package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.CreditorDebtorArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.CredentialKeys;
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

public class DeutscheBankDEAgentPaymentTest {
    private final ArgumentManager<UsernameArgumentEnum> usernameManager =
            new ArgumentManager<>(UsernameArgumentEnum.values());
    private final ArgumentManager<CreditorDebtorArgumentEnum> creditorDebtorManager =
            new ArgumentManager<>(CreditorDebtorArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        creditorDebtorManager.before();
        usernameManager.before();
        builder =
                new AgentIntegrationTest.Builder("de", "de-deutschebank-ob")
                        .addCredentialField(
                                CredentialKeys.USERNAME,
                                usernameManager.get(UsernameArgumentEnum.USERNAME))
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testSepaPayments() throws Exception {
        builder.build().testTinkLinkPayment(createSepaPayment().build());
    }

    @Test
    public void testSepaInstantPayments() throws Exception {
        builder.build().testTinkLinkPayment(createSepaInstantPayment().build());
    }

    @Test
    public void testRecurringPayments() throws Exception {
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
                .withStartDate(LocalDate.now().plusDays(3))
                // has to be the last date of month
                .withEndDate(LocalDate.of(2021, 7, 31))
                .withExecutionRule(ExecutionRule.FOLLOWING)
                .withDayOfMonth(25);
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
                new IbanIdentifier(creditorDebtorManager.get(CreditorDebtorArgumentEnum.CREDITOR));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Tobias Klug");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(CreditorDebtorArgumentEnum.DEBTOR));
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
