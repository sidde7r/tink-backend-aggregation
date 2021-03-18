package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class HandelsbankenAgentPaymentTest {

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("nl", "nl-handelsbanken-ob")
                        .addCredentialField(
                                "accessToken",
                                "QVQ6YTk3NTgyNjYtYzdjZi00ZDcwLTg3MjUtMDEwNGRhMjBlZmM2")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);

        builder.build().testGenericPayment(createListMockedPayment(4));
    }

    private List<Payment> createListMockedPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifierType.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.NL).toString()).when(debtor).getAccountNumber();

            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifierType.IBAN).when(creditor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.NL).toString()).when(creditor).getAccountNumber();

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
