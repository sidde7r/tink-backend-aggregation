package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.agent;

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
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class NordeaNoAgentPaymentTest {

    private final ArgumentManager<NordeaNoAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(NordeaNoAgentPaymentTest.Arg.values());

    enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT,
        CREDITOR_ACCOUNT;

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
                new AgentIntegrationTest.Builder("no", "no-nordea-ob")
                        .setAppId("tink")
                        .setFinancialInstitutionId("nordea")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .dumpContentForContractFile()
                        .expectLoggedIn(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testOneTimePayment() throws Exception {

        builder.build()
                .testTinkLinkPayment(createPayment().withExecutionDate(LocalDate.now()).build());
    }

    private Payment.Builder createPayment() {
        String paymentId = preparePaymentId("Tink");
        System.out.println("Running payment: " + paymentId);

        return createRealPayment(paymentId)
                .withPaymentScheme(PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER);
    }

    private Payment.Builder createRealPayment(String paymentId) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(paymentId);
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier =
                new BbanIdentifier(creditorDebtorManager.get(Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier =
                new BbanIdentifier(creditorDebtorManager.get(Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inNOK(5);

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
