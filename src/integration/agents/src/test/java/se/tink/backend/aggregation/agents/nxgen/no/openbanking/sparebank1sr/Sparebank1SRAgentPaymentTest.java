package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1sr;

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
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class Sparebank1SRAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<Sparebank1SRAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(Sparebank1SRAgentPaymentTest.Arg.values());

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT,
        CREDITOR_ACCOUNT;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    @Before
    public void setup() {
        creditorDebtorManager.before();
        builder =
                new AgentIntegrationTest.Builder("no", "no-sparebank1sr-ob")
                        .setFinancialInstitutionId("sparebank1")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
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

    private Payment.Builder createPayment() {
        String remittanceInformationValue = prepareRemittanceInfo("Tink-Test");
        System.out.println("Running payment: " + remittanceInformationValue);

        return createRealDomesticPayment(remittanceInformationValue)
                .withPaymentScheme(PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER);
    }

    private Payment.Builder createPayment(String uniqueId) {
        String remittanceInformationValue = prepareRemittanceInfo("Tink-Test");
        System.out.println("Running payment: " + remittanceInformationValue);

        return createRealDomesticPayment(remittanceInformationValue, uniqueId)
                .withPaymentScheme(PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER);
    }

    private Payment.Builder createRecurringPayment() {
        String paymentId = prepareRemittanceInfo("Tink-Recurring");
        System.out.println("Running payment: " + paymentId);

        return createRealDomesticPayment(paymentId)
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withPaymentScheme(PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER)
                .withStartDate(LocalDate.now().plus(1, ChronoUnit.DAYS))
                .withEndDate(LocalDate.now().plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.MONTHS))
                .withFrequency(Frequency.MONTHLY)
                .withExecutionDate(LocalDate.now().plus(1, ChronoUnit.DAYS));
    }

    private Payment.Builder createRealDomesticPayment(String remittanceInformationValue) {
        return createRealDomesticPayment(remittanceInformationValue, UUID.randomUUID().toString());
    }

    private Payment.Builder createRealDomesticPayment(
            String remittanceInformationValue, String uniqueId) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(remittanceInformationValue);
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier debtorAccountIdentifier =
                new NorwegianIdentifier(creditorDebtorManager.get(Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        AccountIdentifier creditorAccountIdentifier =
                new NorwegianIdentifier(
                        creditorDebtorManager.get(
                                Sparebank1SRAgentPaymentTest.Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        ExactCurrencyAmount amount = ExactCurrencyAmount.inNOK(2);

        return new Payment.Builder()
                .withPaymentServiceType(PaymentServiceType.SINGLE)
                .withDebtor(debtor)
                .withCreditor(creditor)
                .withExactCurrencyAmount(amount)
                .withCurrency("NOK")
                .withUniqueId(uniqueId)
                .withRemittanceInformation(remittanceInformation);
    }

    private String prepareRemittanceInfo(String prefix) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss");
        String dateNow = LocalDateTime.now().format(formatter);
        return prefix + "-" + dateNow;
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
