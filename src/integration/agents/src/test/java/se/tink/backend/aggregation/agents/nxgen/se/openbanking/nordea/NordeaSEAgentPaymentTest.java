package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@Ignore
public class NordeaSEAgentPaymentTest {

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("SE", "se-nordea-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testPaymentRevamp(createListMockedDomesticPayment(4));
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

            Amount amount = Amount.inSEK(new Random().nextInt(50000));
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
}
