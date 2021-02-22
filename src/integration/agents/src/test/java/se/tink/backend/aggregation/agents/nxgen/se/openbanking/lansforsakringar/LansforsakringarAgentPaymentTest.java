package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

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
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class LansforsakringarAgentPaymentTest {

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("SE", "se-lansforsakringar-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPayment(createListMockedDomesticPayment(4));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(Type.SE).when(creditor).getAccountIdentifierType();
            doReturn("222222222222").when(creditor).getAccountNumber();
            doReturn("Anna Andersson").when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.SE).when(debtor).getAccountIdentifierType();
            doReturn("11111111111").when(debtor).getAccountNumber();

            ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(new Random().nextInt(50000));
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
}
