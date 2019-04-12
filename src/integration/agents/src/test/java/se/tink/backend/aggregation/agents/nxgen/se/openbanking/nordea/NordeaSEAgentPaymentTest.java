package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

import java.time.LocalDate;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

//@Ignore
public class NordeaSEAgentPaymentTest {

    @Test
    public void testRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("SE", "se-nordea-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);


        builder.build().testPaymentRevamp(createMockedDomesticPayment());
    }

    private Payment createMockedDomesticPayment() {
        Creditor creditor = mock(Creditor.class);
        doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
        doReturn("SE4550000000058398257466").when(creditor).getAccountNumber();
        doReturn("SEK").when(creditor).getCurrency();
        doReturn("Jack Bauer").when(creditor).getCreditorName();
        doReturn("24").when(creditor).getMessageToCreditor();


        Debtor debtor = mock(Debtor.class);
        doReturn(AccountIdentifier.Type.SE).when(debtor).getAccountIdentifierType();
        doReturn("41351300039").when(debtor).getAccountNumber();
        doReturn("SEK").when(debtor).getCurrency();
        doReturn("To Jack").when(debtor).getOwnMessage();

        Amount amount = Amount.inSEK(10);
        LocalDate executionDate = LocalDate.now();
        String currency = "SEK";

        return new Payment(creditor, debtor, amount, executionDate, currency);
    }
}
