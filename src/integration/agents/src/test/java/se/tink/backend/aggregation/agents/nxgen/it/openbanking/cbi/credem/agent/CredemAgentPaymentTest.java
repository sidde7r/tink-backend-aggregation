package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.credem.agent;

import java.time.LocalDate;
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
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class CredemAgentPaymentTest {
    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<ArgumentManager.PsuIdArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.PsuIdArgumentEnum.values());
    private final ArgumentManager<CredemAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(CredemAgentPaymentTest.Arg.values());

    @Before
    public void setup() throws Exception {
        builder =
                new AgentIntegrationTest.Builder("it", "it-credem-oauth2")
                        .setFinancialInstitutionId("cbi")
                        .setAppId("tink")
                        .setOriginatingUserIp(System.getProperty("tink.ORIGINATING_USER_IP"))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testPayments() throws Exception {
        manager.before();
        creditorDebtorManager.before();

        builder.build().testTinkLinkPayment(createRealDomesticPayment().build());
    }

    @Test
    public void testRecurringPayments() throws Exception {
        manager.before();
        creditorDebtorManager.before();

        builder.build().testTinkLinkPayment(createRealDomesticRecurringPayment().build());
    }

    private Payment.Builder createRealDomesticRecurringPayment() {
        return createRealDomesticPayment()
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withFrequency(Frequency.MONTHLY)
                .withStartDate(LocalDate.of(2021, 7, 16))
                .withEndDate(LocalDate.of(2021, 10, 16))
                .withExecutionRule(ExecutionRule.FOLLOWING);
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("CredemAgent");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);
        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1.00);
        LocalDate executionDate = LocalDate.now();

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(amount.getCurrencyCode())
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER);
    }

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT, // Domestic IBAN account number
        CREDITOR_ACCOUNT; // Domestic IBAN account number

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
