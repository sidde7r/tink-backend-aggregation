package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PsuIdArgumentEnum;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class DnbAgentPaymentTest {

    private final ArgumentManager<PsuIdArgumentEnum> manager =
            new ArgumentManager<>(PsuIdArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(4));
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("no", "no-dnb-ob")
                        .addCredentialField("PSU-ID", manager.get(PsuIdArgumentEnum.PSU_ID))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(Type.IBAN).when(creditor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.DK).toString()).when(creditor).getAccountNumber();
            doReturn("John Smith").when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.NO).toString()).when(debtor).getAccountNumber();

            Amount amount = Amount.inNOK(new Random().nextInt(50000));
            LocalDate executionDate = LocalDate.now();
            String currency = "NOK";

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
