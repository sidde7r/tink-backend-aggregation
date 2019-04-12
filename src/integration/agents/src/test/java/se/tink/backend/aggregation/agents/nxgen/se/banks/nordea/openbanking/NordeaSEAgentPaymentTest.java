package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.openbanking;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;

@Ignore
public class NordeaSEAgentPaymentTest {
    private static String SANDBOX_SE_TEST_USER = "";

    @Test
    public void testRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "se-nordea-openbanking")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true)
                        .addCredentialField(Field.Key.USERNAME, SANDBOX_SE_TEST_USER)
                        .expectLoggedIn(false);


        Creditor creditor = mock(Creditor.class);
        Debtor debtor = mock(Debtor.class);
        Amount amount = Amount.inSEK(10);
        LocalDate executionDate = LocalDate.now();
        String currency = "SEK";

        Payment payment = new Payment(creditor, debtor, amount, executionDate, currency);
        builder.build().testPaymentRevamp(payment);
    }
}
