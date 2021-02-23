package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

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
        loadBeforeSaveAfterManager.before();
        creditorDebtorManager.before();
        ssnManager.before();
        builder =
                new AgentIntegrationTest.Builder("SE", "se-nordea-ob")
                        .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                        .expectLoggedIn(false)
                        .setAppId("tink")
                        .loadCredentialsBefore(
                                Boolean.parseBoolean(
                                        loadBeforeSaveAfterManager.get(
                                                LoadBeforeSaveAfterArgumentEnum.LOAD_BEFORE)))
                        .saveCredentialsAfter(
                                Boolean.parseBoolean(
                                        loadBeforeSaveAfterManager.get(
                                                LoadBeforeSaveAfterArgumentEnum.SAVE_AFTER)));
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createRealDomesticPayment());
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
        LocalDate executionDate = LocalDate.now();
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
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.SE).toString()).when(creditor).getAccountNumber();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.SE).when(debtor).getAccountIdentifierType();
            doReturn("41351300039").when(debtor).getAccountNumber();

            ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(1);
            LocalDate executionDate = LocalDate.now();
            String currency = "SEK";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
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
