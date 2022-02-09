package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.agent;

import java.time.LocalDate;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.CreditorDebtorArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PasswordArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
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

public class UnicreditAgentPaymentTest {

    private final ArgumentManager<UsernameArgumentEnum> usernameManager =
            new ArgumentManager<>(UsernameArgumentEnum.values());
    private final ArgumentManager<PasswordArgumentEnum> passwordManager =
            new ArgumentManager<>(PasswordArgumentEnum.values());
    private final ArgumentManager<CreditorDebtorArgumentEnum> creditorDebtorManager =
            new ArgumentManager<>(CreditorDebtorArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        usernameManager.before();
        passwordManager.before();
        creditorDebtorManager.before();

        builder =
                new AgentIntegrationTest.Builder("de", "de-unicredit-ob")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                usernameManager.get(UsernameArgumentEnum.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                passwordManager.get(PasswordArgumentEnum.PASSWORD))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false)
                        .setClusterId("oxford-preprod")
                        .setFinancialInstitutionId("unicredit-de")
                        .setAppId("tink");
    }

    @Test
    public void testSepaPayments() throws Exception {
        builder.build()
                .testTinkLinkPayment(
                        createSepaPayment().withExecutionDate(LocalDate.now()).build());
    }

    @Test
    public void testSepaInstantPayments() throws Exception {
        builder.build()
                .testTinkLinkPayment(
                        createSepaInstantPayment().withExecutionDate(LocalDate.now()).build());
    }

    @Test
    public void testRecurringPayments() throws Exception {
        creditorDebtorManager.before();

        builder.build().testTinkLinkPayment(createRealDomesticRecurringPayment().build());
    }

    private Payment.Builder createRealDomesticRecurringPayment() {
        return createSepaPayment()
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withFrequency(Frequency.MONTHLY)
                .withStartDate(LocalDate.now().plusDays(2))
                .withEndDate(LocalDate.now().plusMonths(3))
                .withExecutionRule(ExecutionRule.FOLLOWING)
                .withDayOfMonth(10);
    }

    private Payment.Builder createSepaPayment() {
        return createRealDomesticPayment().withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER);
    }

    private Payment.Builder createSepaInstantPayment() {
        return createRealDomesticPayment()
                .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER);
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("ReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.REFERENCE);

        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(CreditorDebtorArgumentEnum.CREDITOR));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

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

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
