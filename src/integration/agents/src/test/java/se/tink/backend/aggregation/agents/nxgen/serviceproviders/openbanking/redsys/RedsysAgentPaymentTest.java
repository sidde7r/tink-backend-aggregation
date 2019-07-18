package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class RedsysAgentPaymentTest {

    private static final String DEBTOR_IBAN = "ES5140000001050000000001";
    private static final String CREDITOR_NAME = "Fulano de Tal";

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("ES", "es-redsys-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        final List<Payment> payments = new ArrayList<>();
        payments.add(createPayment(DEBTOR_IBAN, 333.0));
        builder.build().testGenericPayment(payments);
    }

    private Payment createPayment(String debtorIban, double amountInEur) {
        final Creditor creditor = mock(Creditor.class);
        doReturn(Type.IBAN).when(creditor).getAccountIdentifierType();
        doReturn(Iban.random(CountryCode.ES).toString()).when(creditor).getAccountNumber();
        doReturn(CREDITOR_NAME).when(creditor).getName();

        final Debtor debtor = mock(Debtor.class);
        doReturn(Type.IBAN).when(debtor).getAccountIdentifierType();
        doReturn(debtorIban).when(debtor).getAccountNumber();

        final Amount amount = Amount.inEUR(amountInEur);

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withAmount(amount)
                .withCurrency(amount.getCurrency())
                .build();
    }
}
