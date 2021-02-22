package se.tink.backend.aggregation.agents.nxgen.no.openbanking.handelsbanken;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class HandelsbankenPaymentAgentTest {
    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("no", "no-handelsbanken-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPayment(createListMockedDomesticPayment(1));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(Type.NO).when(creditor).getAccountIdentifierType();
            doReturn("NO4594830601990").when(creditor).getAccountNumber();
            doReturn("Victoria Helene Romberg").when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(Type.NO).when(debtor).getAccountIdentifierType();
            doReturn("NO1594830638614").when(debtor).getAccountNumber();

            ExactCurrencyAmount amount = ExactCurrencyAmount.inNOK(10);
            LocalDate executionDate = LocalDate.now();
            String currency = "NOK";

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
