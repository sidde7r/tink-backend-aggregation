package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class BunqAgentPaymentTest {

    private enum Arg implements ArgumentManagerEnum {
        CREDITOR_IBAN,
        DEDTOR_IBAN;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> loadBeforeSaveAfterManager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void before() {
        manager.before();
        loadBeforeSaveAfterManager.before();

        builder =
                new AgentIntegrationTest.Builder("nl", "nl-bunq-oauth2")
                        .loadCredentialsBefore(
                                Boolean.parseBoolean(
                                        loadBeforeSaveAfterManager.get(
                                                LoadBeforeSaveAfterArgumentEnum.LOAD_BEFORE)))
                        .saveCredentialsAfter(
                                Boolean.parseBoolean(
                                        loadBeforeSaveAfterManager.get(
                                                LoadBeforeSaveAfterArgumentEnum.SAVE_AFTER)))
                        .setFinancialInstitutionId("bunq")
                        .setAppId("tink");
    }

    @Test
    public void testPayments() throws Exception {
        builder.expectLoggedIn(false)
                .build()
                .testGenericPayment(createListMockedDomesticPayment(1));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
            doReturn(manager.get(Arg.CREDITOR_IBAN)).when(creditor).getAccountNumber();
            doReturn("bunq account").when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(manager.get(Arg.DEDTOR_IBAN)).when(debtor).getAccountNumber();

            Amount amount = Amount.inEUR(new Random().nextInt(50));
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }
}
