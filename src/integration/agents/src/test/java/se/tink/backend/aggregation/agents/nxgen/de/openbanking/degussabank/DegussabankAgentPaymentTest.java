package se.tink.backend.aggregation.agents.nxgen.de.openbanking.degussabank;

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
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DegussabankAgentPaymentTest {

    private final ArgumentManager<ArgumentManager.UsernamePasswordArgumentEnum>
            usernamePasswordManager =
                    new ArgumentManager<>(ArgumentManager.UsernamePasswordArgumentEnum.values());
    private final ArgumentManager<DegussabankAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(DegussabankAgentPaymentTest.Arg.values());
    private static final String TRANSFER_DATE_FORMAT = "yyyy/MM/dd 'at' HH:mm";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        usernamePasswordManager.before();
        creditorDebtorManager.before();

        builder =
                new AgentIntegrationTest.Builder("de", "de-degussabank-ob")
                        .addCredentialField(
                                Key.USERNAME,
                                usernamePasswordManager.get(
                                        ArgumentManager.UsernamePasswordArgumentEnum.USERNAME))
                        .addCredentialField(
                                Key.PASSWORD,
                                usernamePasswordManager.get(
                                        ArgumentManager.UsernamePasswordArgumentEnum.PASSWORD))
                        .setFinancialInstitutionId("f76b2c92bef511eb85290242ac130003")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT,
        CREDITOR_ACCOUNT;

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
        builder.build().testTinkLinkPayment(createRecurringPayment().build());
    }

    private Payment.Builder createRecurringPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        setUnstructuredRemittanceInformation(remittanceInformation);
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = startDate.plusMonths(2);
        int transferDayOfMonth = startDate.getDayOfMonth();

        return createSepaPayment()
                .withRemittanceInformation(remittanceInformation)
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withFrequency(Frequency.MONTHLY)
                .withStartDate(startDate)
                .withEndDate(endDate)
                .withDayOfMonth(transferDayOfMonth);
    }

    private Payment.Builder createSepaPayment() {
        return createRealDomesticPayment().withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER);
    }

    private Payment.Builder createSepaInstantPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        setUnstructuredRemittanceInformation(remittanceInformation);

        return createRealDomesticPayment()
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER);
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        setUnstructuredRemittanceInformation(remittanceInformation);

        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(
                                DegussabankAgentPaymentTest.Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Test Creditor");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(DegussabankAgentPaymentTest.Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        String currency = "EUR";

        return new Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation);
    }

    private void setUnstructuredRemittanceInformation(RemittanceInformation remittanceInformation) {
        remittanceInformation.setValue(getRemittanceInformation());
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
    }

    private String getRemittanceInformation() {
        return "SepaReferenceToCreditor "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern(TRANSFER_DATE_FORMAT));
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
