package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

public class SwedbankAgentPaymentTest {

    private final ArgumentManager<Arg> creditorDebtorManager = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> loadBeforeSaveAfterManager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        creditorDebtorManager.before();
        loadBeforeSaveAfterManager.before();

        builder =
                new AgentIntegrationTest.Builder("se", "se-swedbank-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("swedbank")
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

        builder.build().testGenericPayment(createListMockedPayment(1));
    }

    private List<Payment> createListMockedPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
            doReturn(creditorDebtorManager.get(Arg.CREDITOR_ACCOUNT))
                    .when(creditor)
                    .getAccountNumber();
            doReturn(creditorDebtorManager.get(Arg.CREDITOR_NAME)).when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(creditorDebtorManager.get(Arg.DEBTOR_ACCOUNT)).when(debtor).getAccountNumber();

            Amount amount = Amount.inSEK(3);
            LocalDate executionDate = LocalDate.now();
            String currency = "SEK";

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

    private enum Arg implements ArgumentManagerEnum {
        DEBTOR_ACCOUNT, // Domestic Swedish account number
        CREDITOR_ACCOUNT, // Domestic Swedish account number
        CREDITOR_NAME; // Domestic Swedish account number

        @Override
        public boolean isOptional() {
            return false;
        }
    }
}
