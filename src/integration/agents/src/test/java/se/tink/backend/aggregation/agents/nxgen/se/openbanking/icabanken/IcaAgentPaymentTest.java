package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class IcaAgentPaymentTest {
    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> manager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("se", "se-icabanken-ob")
                        .loadCredentialsBefore(
                                Boolean.parseBoolean(
                                        manager.get(LoadBeforeSaveAfterArgumentEnum.LOAD_BEFORE)))
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(
                                Boolean.parseBoolean(
                                        manager.get(LoadBeforeSaveAfterArgumentEnum.SAVE_AFTER)));
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(4));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn("name").when(creditor).getName();
            doReturn(AccountIdentifierType.IBAN).when(creditor).getAccountIdentifierType();
            doReturn("SE24927000000000001234567").when(creditor).getAccountNumber();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifierType.IBAN).when(debtor).getAccountIdentifierType();
            doReturn("SE24927000000000001234567").when(debtor).getAccountNumber();

            ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(new Random().nextInt(100));
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

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
}
