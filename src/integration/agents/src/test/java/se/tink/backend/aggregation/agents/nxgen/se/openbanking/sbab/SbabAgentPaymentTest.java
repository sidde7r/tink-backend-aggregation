package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class SbabAgentPaymentTest {

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("SE", "se-sbab-oauth2")
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
            doReturn(RandomStringUtils.random(20, false, true)).when(creditor).getAccountNumber();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.SE).when(debtor).getAccountIdentifierType();
            doReturn(RandomStringUtils.random(20, false, true)).when(debtor).getAccountNumber();
            Amount amount = Amount.inSEK(new Random().nextInt(40000));
            LocalDate executionDate = LocalDate.now();
            String currency = "SEK";

            Payment payment =
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .build();

            listOfMockedPayments.add(payment);
        }

        return listOfMockedPayments;
    }
}
