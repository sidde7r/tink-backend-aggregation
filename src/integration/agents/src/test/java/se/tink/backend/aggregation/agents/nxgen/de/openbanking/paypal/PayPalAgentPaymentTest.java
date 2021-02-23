package se.tink.backend.aggregation.agents.nxgen.de.openbanking.paypal;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class PayPalAgentPaymentTest {

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("de", "de-paypal-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPayment(createListMockedDomesticPayment(4));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Debtor debtor = mock(Debtor.class);
            doReturn("payee@example.com").when(debtor).getAccountNumber();
            doReturn(Type.TINK).when(debtor).getAccountIdentifierType();

            String currency = "EUR";
            ExactCurrencyAmount amount = ExactCurrencyAmount.of(randomAmount(), currency);

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(amount)
                            .withCurrency(currency)
                            .withType(PaymentType.INTERNATIONAL)
                            .build());
        }

        return listOfMockedPayments;
    }

    private int randomAmount() {
        return 1 + new Random().nextInt(99);
    }
}
