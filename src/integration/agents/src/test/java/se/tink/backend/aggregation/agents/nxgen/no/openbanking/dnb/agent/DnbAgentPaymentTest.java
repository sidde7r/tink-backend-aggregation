package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.agent;

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

public class DnbAgentPaymentTest {

    private final ArgumentManager<Arg> creditorDebtorManager = new ArgumentManager<>(Arg.values());

    enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT, // IBAN account number
        CREDITOR_ACCOUNT, // IBAN account number
        PSU_ID; // User SSN

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
                new AgentIntegrationTest.Builder("no", "no-dnb-ob")
                        .setAppId("tink")
                        .addCredentialField("PSU-ID", creditorDebtorManager.get(Arg.PSU_ID))
                        .setFinancialInstitutionId("dnb")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testPayment() throws Exception {
        builder.build()
                .testTinkLinkPayment(createPayment().withExecutionDate(LocalDate.now()).build());
    }

    @Test
    public void testRecurringPayment() throws Exception {
        builder.build().testTinkLinkPayment(createRecurringPayment().build());
    }

    @Test
    public void testInstantPayment() throws Exception {
        builder.build()
                .testTinkLinkPayment(
                        createInstantPayment().withExecutionDate(LocalDate.now()).build());
    }

    private Payment.Builder createPayment() {
        String paymentId = preparePaymentId("Tink");
        System.out.println("Running payment: " + paymentId);

        return createRealPayment(paymentId)
                .withPaymentScheme(PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER);
    }

    private Payment.Builder createRecurringPayment() {
        String paymentId = preparePaymentId("TinkRecurring");
        System.out.println("Running payment: " + paymentId);

        return createRealPayment(paymentId)
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withPaymentScheme(PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER)
                .withStartDate(LocalDate.now().plus(1, ChronoUnit.DAYS))
                .withEndDate(LocalDate.now().plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.MONTHS))
                .withFrequency(Frequency.MONTHLY)
                .withExecutionDate(LocalDate.now().plus(1, ChronoUnit.DAYS));
    }

    private Payment.Builder createInstantPayment() {
        String paymentId = preparePaymentId("TinkInstant");
        System.out.println("Running payment: " + paymentId);

        return createRealPayment(paymentId)
                .withPaymentScheme(PaymentScheme.INSTANT_NORWEGIAN_DOMESTIC_CREDIT_TRANSFER_STRAKS);
    }

    private Payment.Builder createRealPayment(String paymentId) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(paymentId);
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inNOK(0.01);

        return new Payment.Builder()
                .withPaymentServiceType(PaymentServiceType.SINGLE)
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency("NOK")
                .withUniqueId(UUID.randomUUID().toString())
                .withRemittanceInformation(remittanceInformation);
    }

    private String preparePaymentId(String prefix) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss");
        String dateNow = LocalDateTime.now().format(formatter);
        return prefix + "-" + dateNow;
    }
}
