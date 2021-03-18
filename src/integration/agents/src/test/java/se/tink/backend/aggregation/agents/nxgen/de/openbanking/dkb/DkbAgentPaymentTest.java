package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.CredentialKeys;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class DkbAgentPaymentTest {
    private final String TEST_USERNAME = "curakec";
    private final String TEST_PASSWORD = "Curakec_1";
    private final String TEST_IBAN = "FR7612345987650123456789014";

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("DE", "de-dkb-ob")
                        .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                        .addCredentialField(CredentialKeys.IBAN, TEST_IBAN)
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPayment(createListMockedDomesticPayment(4));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = Mockito.mock(Creditor.class);
            Mockito.doReturn(AccountIdentifierType.IBAN).when(creditor).getAccountIdentifierType();
            Mockito.doReturn(Iban.random(CountryCode.DE).toString())
                    .when(creditor)
                    .getAccountNumber();
            Mockito.doReturn("Creditor Name").when(creditor).getName();
            Debtor debtor = Mockito.mock(Debtor.class);
            Mockito.doReturn(AccountIdentifierType.IBAN).when(debtor).getAccountIdentifierType();
            Mockito.doReturn(Iban.random(CountryCode.DE).toString())
                    .when(debtor)
                    .getAccountNumber();

            ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(new Random().nextInt(50000));
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

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
