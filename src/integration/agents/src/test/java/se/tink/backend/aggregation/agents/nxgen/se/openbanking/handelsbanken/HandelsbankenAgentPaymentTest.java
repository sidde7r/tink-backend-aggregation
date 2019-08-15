package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenAgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class HandelsbankenAgentPaymentTest {

    @Test
    public void testPayments() throws Exception {
        HandelsbankenAgentIntegrationTest.Builder builder =
                new HandelsbankenAgentIntegrationTest.Builder("se", "se-handelsbanken-oauth2")
                        .addCredentialField(
                                "accessToken",
                                "QVQ6OTMzZWYwNTctZDE5MC00MDhmLThjOTgtYjY3OGFiM2I1ZDZj")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);

        builder.build().testGenericPayment(createListMockedPayment(2));
    }

    private List<Payment> createListMockedPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.SE).when(debtor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.SE).toString()).when(debtor).getAccountNumber();

            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifier.Type.SE).when(creditor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.SE).toString()).when(creditor).getAccountNumber();

            Amount amount = Amount.inSEK(new Random().nextInt(50000));
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
