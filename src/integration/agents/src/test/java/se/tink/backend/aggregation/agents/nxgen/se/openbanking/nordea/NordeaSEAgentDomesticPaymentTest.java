package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

// DISCLAIMER! Actual money being transferred, run under own responsibility
public class NordeaSEAgentDomesticPaymentTest {

    private final ArgumentManager<Arg> creditorDebtorManager = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> loadBeforeSaveAfterManager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        /*        loadBeforeSaveAfterManager.before();
        creditorDebtorManager.before();
        ssnManager.before();*/
        builder =
                new AgentIntegrationTest.Builder("se", "se-nordea-ob")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("nordea")
                        .setAppId("tink")
                        .setClusterId("oxford-preprod")
                        .expectLoggedIn(false);
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(1));
    }

    @Test
    public void testCancelPayments() throws Exception {
        builder.build().testCancelPayment(createCancellablePayment());
    }

    private Payment createCancellablePayment() {
        return new Payment.Builder()
                .withUniqueId("4ba82f91-93a1-4878-8d41-18eeb123c8a0")
                .withType(PaymentType.DOMESTIC)
                .build();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private List<Payment> createRealDomesticPayment() {
        AccountIdentifier creditorAccountIdentifier =
                new SwedishIdentifier(creditorDebtorManager.get(Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier);

        AccountIdentifier debtorAccountIdentifier =
                new SwedishIdentifier(creditorDebtorManager.get(Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(1);
        LocalDate executionDate = LocalDate.now().plusDays(7);
        String currency = "SEK";

        return Arrays.asList(
                new Payment.Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withExactCurrencyAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .build());
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = new Creditor(new SwedishIdentifier("90255481251"), "Ash");

            Debtor debtor = new Debtor(new SwedishIdentifier("33820004238"));

            ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(1);
            LocalDate executionDate = LocalDate.now().plusDays(2);
            String currency = "SEK";

            RemittanceInformation remittanceInformation = new RemittanceInformation();
            remittanceInformation.setValue("Message");

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .withRemittanceInformation(remittanceInformation)
                            .build());
        }

        return listOfMockedPayments;
    }

    private enum Arg implements ArgumentManagerEnum {
        DEBTOR_ACCOUNT, // Domestic Swedish account number
        CREDITOR_ACCOUNT; // Domestic Swedish account number

        @Override
        public boolean isOptional() {
            return false;
        }
    }
}
