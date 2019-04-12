package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.openbanking;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.rpc.Account;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

import java.time.LocalDate;

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

        Creditor creditor = new Creditor(new Account());
        Debtor debtor = new Debtor(new Account());
        Amount amount = Amount.inSEK(10);
        LocalDate executionDate = LocalDate.now();

        Payment payment = new Payment(creditor, debtor, amount, executionDate);
        builder.build().testPaymentRevamp(payment);
    }
}
