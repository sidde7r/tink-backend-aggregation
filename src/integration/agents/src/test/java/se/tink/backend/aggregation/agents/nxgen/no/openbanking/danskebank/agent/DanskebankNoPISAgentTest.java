package se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank.agent;

import java.math.BigDecimal;
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

public class DanskebankNoPISAgentTest {

    private final ArgumentManager<Arg> creditorDebtorManager = new ArgumentManager<>(Arg.values());
    private static final String FINANCIAL_INSTITUTION_ID = "bf1f1d0ad7a04c64a32b998c4d2cd2bb";
    private AgentIntegrationTest.Builder builder;

    enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT, // IBAN account number
        CREDITOR_ACCOUNT; // IBAN account number

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    @Before
    public void setup() {
        creditorDebtorManager.before();

        builder =
                new AgentIntegrationTest.Builder("no", "no-danskebank-ob")
                        .setAppId("tink")
                        .setFinancialInstitutionId(FINANCIAL_INSTITUTION_ID)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testDomesticPaymentWithUnstructuredInfo() throws Exception {
        builder.build()
                .testGenericPaymentUKOB(
                        createPayment(false).withExecutionDate(LocalDate.now()).build());
    }

    @Test
    public void testDomesticPaymentWithStructuredInfo() throws Exception {
        builder.build()
                .testGenericPaymentUKOB(
                        createPayment(true).withExecutionDate(LocalDate.now()).build());
    }

    private Payment.Builder createPayment(boolean isStructured) {
        String paymentId = preparePaymentId();
        System.out.println("Running payment: " + paymentId);

        return createRealPayment(paymentId, isStructured)
                .withPaymentScheme(PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER);
    }

    private Payment.Builder createRealPayment(String paymentId, boolean isStructured) {

        RemittanceInformation remittanceInformation = new RemittanceInformation();

        if (isStructured) {
            remittanceInformation.setType(RemittanceInformationType.REFERENCE);
            // put the real KID number when testing
            remittanceInformation.setValue("211333113332200900022");
        } else {
            remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
            remittanceInformation.setValue(paymentId);
        }

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(creditorDebtorManager.get(Arg.CREDITOR_ACCOUNT));

        Creditor creditor;
        if (isStructured) {
            // change the name to real merchant when testing
            creditor = new Creditor(creditorAccountIdentifier, "Merchant bank account name");
        } else {
            creditor = new Creditor(creditorAccountIdentifier, "Tink Test");
        }

        ExactCurrencyAmount amount = new ExactCurrencyAmount(BigDecimal.valueOf(1.00), "NOK");

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency("NOK")
                .withUniqueId(UUID.randomUUID().toString().replace("-", ""))
                .withRemittanceInformation(remittanceInformation);
    }

    private String preparePaymentId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
        String dateNow = LocalDateTime.now().format(formatter);
        return "TinkTest-" + dateNow;
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
