package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.agent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
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
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SparkassenAgentPaymentTest {
    private final ArgumentManager<ArgumentManager.UsernamePasswordArgumentEnum>
            usernamePasswordManager =
                    new ArgumentManager<>(ArgumentManager.UsernamePasswordArgumentEnum.values());
    private final ArgumentManager<SparkassenAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(SparkassenAgentPaymentTest.Arg.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        usernamePasswordManager.before();
        creditorDebtorManager.before();

        builder =
                new AgentIntegrationTest.Builder("de", "de-sparkassestadmünchen-ob")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                usernamePasswordManager.get(
                                        ArgumentManager.UsernamePasswordArgumentEnum.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                usernamePasswordManager.get(
                                        ArgumentManager.UsernamePasswordArgumentEnum.PASSWORD))
                        .setFinancialInstitutionId("sparkassen")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT, // Domestic IBAN account number
        CREDITOR_ACCOUNT; // Domestic IBAN account number

        @Override
        public boolean isOptional() {
            return false;
        }
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
        usernamePasswordManager.before();
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
                .withDayOfExecution(10);
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
                        creditorDebtorManager.get(SparkassenAgentPaymentTest.Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(SparkassenAgentPaymentTest.Arg.DEBTOR_ACCOUNT));
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

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
