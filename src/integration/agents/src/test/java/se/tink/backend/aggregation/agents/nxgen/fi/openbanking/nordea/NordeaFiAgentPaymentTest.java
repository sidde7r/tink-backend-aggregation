package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class NordeaFiAgentPaymentTest {

    private final ArgumentManager<Arg> creditorDebtorManager = new ArgumentManager<>(Arg.values());

    enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT, // IBAN account number
        CREDITOR_ACCOUNT; // IBAN account number

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        creditorDebtorManager.before();

        builder =
                new AgentIntegrationTest.Builder("fi", "fi-nordea-oauth2")
                        .setAppId("tink")
                        .setFinancialInstitutionId("a445338564ea4940bfcc4128a1ddc2d8")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testSepaPayment() throws Exception {
        builder.build()
                .testTinkLinkPayment(
                        createSepaPayment().withExecutionDate(LocalDate.now()).build());
    }

    @Test
    public void testSepaRecurringPayment() throws Exception {
        builder.build().testTinkLinkPayment(createSepaRecurringPayment().build());
    }

    private Payment.Builder createSepaPayment() {
        String paymentId = preparePaymentId("TinkSepa");
        System.out.println("Running payment: " + paymentId);

        return createRealSepaPayment(paymentId)
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER);
    }

    private Payment.Builder createSepaRecurringPayment() {
        String paymentId = preparePaymentId("TinkSepaRecurring");
        System.out.println("Running payment: " + paymentId);

        return createRealSepaPayment(paymentId)
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withStartDate(LocalDate.now().plus(1, ChronoUnit.DAYS))
                .withEndDate(LocalDate.now().plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.MONTHS))
                .withFrequency(Frequency.MONTHLY)
                .withExecutionDate(LocalDate.now().plus(1, ChronoUnit.DAYS));
    }

    private Payment.Builder createRealSepaPayment(String paymentId) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(paymentId);
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(0.01);

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency("EUR")
                .withUniqueId(UUID.randomUUID().toString())
                .withRemittanceInformation(remittanceInformation);
    }

    private String preparePaymentId(String prefix) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss");
        String dateNow = LocalDateTime.now().format(formatter);
        return prefix + "-" + dateNow;
    }
}
