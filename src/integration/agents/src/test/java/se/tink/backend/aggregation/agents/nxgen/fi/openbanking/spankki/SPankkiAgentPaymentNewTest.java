package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SPankkiAgentPaymentNewTest {

    private final ArgumentManager<SPankkiAgentPaymentNewTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(SPankkiAgentPaymentNewTest.Arg.values());

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
                new AgentIntegrationTest.Builder("fi", "fi-spankki-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("spankki")
                        .setAppId("tink");
    }

    @Test
    public void testSepaPayment() throws Exception {
        builder.build()
                .testTinkLinkPayment(
                        createSepaPayment().withExecutionDate(LocalDate.now()).build());
    }

    private Payment.Builder createRealSepaPayment(String paymentId) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(paymentId);
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(SPankkiAgentPaymentNewTest.Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Tink Test");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(SPankkiAgentPaymentNewTest.Arg.DEBTOR_ACCOUNT));
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

    private Payment.Builder createSepaPayment() {
        String paymentId = prepareRemittanceInformation("TinkSepa");
        System.out.println("Running payment: " + paymentId);

        return createRealSepaPayment(paymentId)
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER);
    }

    private String prepareRemittanceInformation(String prefix) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss");
        String dateNow = LocalDateTime.now().format(formatter);
        return prefix + "-" + dateNow;
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
