package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

//@Ignore
public class BecAgentPaymentTest {

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("DK", "dk-bec-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPayment(createListMockedDomesticPayment(4));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifier.Type.DK).when(creditor).getAccountIdentifierType();
            doReturn("59936533150894").when(creditor).getAccountNumber();
            doReturn("Mocked creditor").when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.DK).when(debtor).getAccountIdentifierType();
            doReturn("99524167166569").when(debtor).getAccountNumber();

            Amount amount = Amount.inDKK(new Random().nextInt(50000));
            LocalDate executionDate = LocalDate.now();
            String currency = "DKK";

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
