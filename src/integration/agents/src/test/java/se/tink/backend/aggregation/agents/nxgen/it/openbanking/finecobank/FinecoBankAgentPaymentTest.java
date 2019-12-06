package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class FinecoBankAgentPaymentTest {

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("it", "it-finecobank-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)));
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(1));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = Mockito.mock(Creditor.class);
            Mockito.doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
            Mockito.doReturn(Iban.random().toString()).when(creditor).getAccountNumber();
            Mockito.doReturn("Walter Bianchi").when(creditor).getName();

            Reference reference = Mockito.mock(Reference.class);
            Mockito.doReturn("causale pagamento").when(reference).getValue();

            Debtor debtor = Mockito.mock(Debtor.class);
            Mockito.doReturn(Type.IBAN).when(debtor).getAccountIdentifierType();
            Mockito.doReturn(Iban.random().toString()).when(debtor).getAccountNumber();

            Amount amount = Amount.inEUR(123.5);
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .withReference(reference)
                            .build());
        }

        return listOfMockedPayments;
    }

    private enum Arg {
        SAVE_AFTER,
        LOAD_BEFORE
    }
}
