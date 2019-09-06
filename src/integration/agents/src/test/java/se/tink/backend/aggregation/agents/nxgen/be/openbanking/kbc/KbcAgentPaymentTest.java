package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class KbcAgentPaymentTest {

    AgentIntegrationTest.Builder builder;

    private enum Arg {
        IBAN,
    }

    private final ArgumentManager<KbcAgentPaymentTest.Arg> manager =
            new ArgumentManager<>(KbcAgentPaymentTest.Arg.values());

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("be", "be-kbc-ob")
                        .addCredentialField("IBAN", manager.get(KbcAgentPaymentTest.Arg.IBAN))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(4));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
            doReturn("BE16690375703426").when(creditor).getAccountNumber();
            doReturn("KBC Sandbox User").when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn("BE63380242772874").when(debtor).getAccountNumber();

            Amount amount = Amount.inEUR(new Random().nextInt(500));
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

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
