package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class RaiffeisenAgentPaymentTest {
    private final ArgumentManager<IbanArgumentEnum> manager =
            new ArgumentManager<>(IbanArgumentEnum.values());

    @Test
    public void testPayments() throws Exception {
        manager.before();

        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("at", "at-raiffeisen-ob")
                        .addCredentialField("IBAN", manager.get(IbanArgumentEnum.IBAN))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPayment(createListMockedDomesticPayment(4));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn("name").when(creditor).getName();
            doReturn(Type.IBAN).when(creditor).getAccountIdentifierType();
            doReturn("AT959900000000012476").when(creditor).getAccountNumber();

            Debtor debtor = mock(Debtor.class);
            doReturn(Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn("AT439900000000010017").when(debtor).getAccountNumber();

            ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(new Random().nextInt(100));
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
